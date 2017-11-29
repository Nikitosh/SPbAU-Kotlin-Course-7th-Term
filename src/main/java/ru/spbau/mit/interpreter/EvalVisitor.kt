package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.ParserRuleContext
import ru.spbau.mit.exceptions.InterpretationException
import ru.spbau.mit.exceptions.ScopeException
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser
import java.io.Writer

class EvalVisitor(private val writer: Writer): FunBaseVisitor<Any>() {

    private val scopes : MutableList<Scope> = mutableListOf(Scope())

    init {
        getCurrentScope().defineFunction("println", object: Function {
            override fun apply(visitor: EvalVisitor, arguments: List<Int>): Int {
                writer.write(arguments.joinToString(separator = " ", postfix = "\n"))
                writer.flush()
                return 0
            }
        })
    }

    override fun visitFile(context: FunParser.FileContext): Any {
        return visit(context.block())
    }

    override fun visitBlock(context: FunParser.BlockContext): Any {
        context.statement().forEach {
            val value = visit(it)
            if (value != Unit &&
                    (it.ifStatement() != null || it.returnStatement() != null || it.whileStatement() != null)) {
                return value
            }
        }
        return Unit
    }

    override fun visitBlockWithBraces(context: FunParser.BlockWithBracesContext): Any {
        addScope(Scope(getCurrentScope()))
        val value = visit(context.block())
        removeScope()
        return value
    }

    override fun visitFunctionDeclaration(context: FunParser.FunctionDeclarationContext) {
        val functionName = context.identifier().text
        val parameterNames = context.parameterNames().identifier().map { visit(it) as String }
        val body = context.blockWithBraces()
        getCurrentScope().defineFunction(functionName, FunFunction(parameterNames, body))
    }

    override fun visitVariableDeclaration(context: FunParser.VariableDeclarationContext) {
        try {
            val variableName = visit(context.identifier()) as String
            getCurrentScope().defineVariable(variableName)
            context.expression()?.let {
                getCurrentScope().setVariableValue(variableName, visit(it) as Int)
            }
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitWhileStatement(context: FunParser.WhileStatementContext): Any {
        fun condition() = visit(context.expression()) as Boolean
        while (condition()) {
            val value = visit(context.blockWithBraces())
            if (value !is Unit) {
                return value
            }
        }
        return Unit
    }

    override fun visitIfStatement(context: FunParser.IfStatementContext): Any {
        val condition = visit(context.expression()) as Boolean
        if (condition) {
            return visit(context.blockWithBraces(0))
        }
        if (context.blockWithBraces().size >= 2) {
            return visit(context.blockWithBraces(1))
        }
        return Unit
    }

    override fun visitAssignment(context: FunParser.AssignmentContext) {
        try {
            val variableName = visit(context.identifier()) as String
            val value = visit(context.expression()) as Int
            getCurrentScope().setVariableValue(variableName, value)
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitReturnStatement(context: FunParser.ReturnStatementContext): Any {
        return visit(context.expression())
    }

    override fun visitBinaryOrExpression(context: FunParser.BinaryOrExpressionContext): Any {
        return visit(context.orExpression()) as Boolean || visit(context.andExpression()) as Boolean
    }

    override fun visitBinaryAndExpression(context: FunParser.BinaryAndExpressionContext): Any {
        return visit(context.andExpression()) as Boolean && visit(context.equalityExpression()) as Boolean
    }

    override fun visitBinaryEqualityExpression(context: FunParser.BinaryEqualityExpressionContext): Any {
        val leftValue = visit(context.equalityExpression()) as Int
        val rightValue = visit(context.relationalExpression()) as Int
        return when (context.op.type) {
            FunParser.EQ -> leftValue == rightValue
            FunParser.NEQ -> leftValue != rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryRelationalExpression(context: FunParser.BinaryRelationalExpressionContext): Any {
        val leftValue = visit(context.relationalExpression()) as Int
        val rightValue = visit(context.additiveExpression()) as Int
        return when (context.op.type) {
            FunParser.GT -> leftValue > rightValue
            FunParser.LT -> leftValue < rightValue
            FunParser.GE -> leftValue >= rightValue
            FunParser.LE -> leftValue <= rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryAdditiveExpression(context: FunParser.BinaryAdditiveExpressionContext): Any {
        val leftValue = visit(context.additiveExpression()) as Int
        val rightValue = visit(context.multiplicativeExpression()) as Int
        return when (context.op.type) {
            FunParser.PLUS -> leftValue + rightValue
            FunParser.MINUS -> leftValue - rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryMultiplicativeExpression(context: FunParser.BinaryMultiplicativeExpressionContext): Any {
        val leftValue = visit(context.multiplicativeExpression()) as Int
        val rightValue = visit(context.unaryExpression()) as Int
        return when (context.op.type) {
            FunParser.MUL -> leftValue * rightValue
            FunParser.DIV -> leftValue / rightValue
            FunParser.MOD -> leftValue % rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitUnaryExpression(context: FunParser.UnaryExpressionContext): Any {
        return try {
            when {
                context.functionCall() != null -> visit(context.functionCall())
                context.identifier() != null ->
                    getCurrentScope().getVariableValue(visit(context.identifier()) as String)
                context.literal() != null -> visit(context.literal())
                context.expression() != null -> visit(context.expression())
                else -> throw getException("Unknown operation type found", context)
            }
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitFunctionCall(context: FunParser.FunctionCallContext): Any {
        return try {
            val functionName = context.identifier().text
            val function = getCurrentScope().getFunction(functionName)
            val arguments = context.arguments().expression().map { visit(it) as Int }
            function.apply(this, arguments)
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitIdentifier(context: FunParser.IdentifierContext): Any {
        return when {
            context.Identifier() != null -> context.Identifier().text
            context.InvalidIdentifier() != null -> throw getException("Invalid identifier found", context)
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitLiteral(context: FunParser.LiteralContext): Any {
        return when {
            context.Number() != null -> context.Number().text.toInt()
            context.LeadingZerosNumber() != null -> throw getException("Number with leading zeros found", context)
            else -> throw getException("Unknown operation type found", context)
        }
    }

    fun getCurrentScope() = scopes.last()

    fun addScope(scope: Scope) {
        scopes.add(scope)
    }

    fun removeScope() {
        scopes.removeAt(scopes.size - 1)
    }

    fun getException(error: String, context: ParserRuleContext) =
            InterpretationException(context.getStart().line, error)
}