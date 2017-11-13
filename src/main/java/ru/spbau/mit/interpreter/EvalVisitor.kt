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
        getCurrentScope().defineFunction("println") { arguments ->
            writer.write(arguments.joinToString(separator = " ", postfix = "\n"))
            writer.flush()
            0
        }
    }

    override fun visitFile(context: FunParser.FileContext): Any {
        return context.block().eval()
    }

    override fun visitBlock(context: FunParser.BlockContext): Any {
        context.statement().forEach {
            val value = it.eval(getCurrentScope())
            if (value != Unit &&
                    (it.ifStatement() != null || it.returnStatement() != null || it.whileStatement() != null)) {
                return value
            }
        }
        return Unit
    }

    override fun visitBlockWithBraces(context: FunParser.BlockWithBracesContext): Any {
        return context.block().eval()
    }

    override fun visitFunctionDeclaration(context: FunParser.FunctionDeclarationContext) {
        val functionName = context.identifier().text
        getCurrentScope().defineFunction(functionName) { arguments ->
            val parameterNames = context.parameterNames().identifier().map { it.eval() as String }
            if (arguments.size != parameterNames.size) {
                throw getException("Illegal number of arguments for function $functionName", context)
            }
            val scope = Scope(getCurrentScope())
            for (i in 0 until arguments.size) {
                scope.defineVariable(parameterNames[i])
                scope.setVariableValue(parameterNames[i], arguments[i])
            }
            val value = context.blockWithBraces().eval(scope)
            if (value is Unit) 0 else value.cast<Int>(context)
        }
    }

    override fun visitVariableDeclaration(context: FunParser.VariableDeclarationContext) {
        try {
            val variableName = context.identifier().eval() as String
            getCurrentScope().defineVariable(variableName)
            context.expression()?.let {
                getCurrentScope().setVariableValue(variableName, it.eval().cast<Int>(context))
            }
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitWhileStatement(context: FunParser.WhileStatementContext): Any {
        fun condition() = context.expression().eval().cast<Boolean>(context)
        while (condition()) {
            val value = context.blockWithBraces().eval()
            if (value !is Unit) {
                return value
            }
        }
        return Unit
    }

    override fun visitIfStatement(context: FunParser.IfStatementContext): Any {
        val condition = context.expression().eval().cast<Boolean>(context)
        if (condition) {
            return context.blockWithBraces(0).eval()
        }
        if (context.blockWithBraces().size >= 2) {
            return context.blockWithBraces(1).eval()
        }
        return Unit
    }

    override fun visitAssignment(context: FunParser.AssignmentContext) {
        try {
            val value = context.expression().eval().cast<Int>(context)
            getCurrentScope().setVariableValue(context.identifier().eval() as String, value)
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitReturnStatement(context: FunParser.ReturnStatementContext): Any {
        return context.expression().eval()
    }

    override fun visitBinaryOrExpression(context: FunParser.BinaryOrExpressionContext): Any {
        return context.orExpression().eval().cast<Boolean>(context)
                || context.andExpression().eval().cast<Boolean>(context)
    }

    override fun visitBinaryAndExpression(context: FunParser.BinaryAndExpressionContext): Any {
        return context.andExpression().eval().cast<Boolean>(context)
                && context.equalityExpression().eval().cast<Boolean>(context)
    }

    override fun visitBinaryEqualityExpression(context: FunParser.BinaryEqualityExpressionContext): Any {
        val leftValue = context.equalityExpression().eval().cast<Int>(context)
        val rightValue = context.relationalExpression().eval().cast<Int>(context)
        return when (context.op.type) {
            FunParser.EQ -> leftValue == rightValue
            FunParser.NEQ -> leftValue != rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryRelationalExpression(context: FunParser.BinaryRelationalExpressionContext): Any {
        val leftValue = context.relationalExpression().eval().cast<Int>(context)
        val rightValue = context.additiveExpression().eval().cast<Int>(context)
        return when (context.op.type) {
            FunParser.GT -> leftValue > rightValue
            FunParser.LT -> leftValue < rightValue
            FunParser.GE -> leftValue >= rightValue
            FunParser.LE -> leftValue <= rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryAdditiveExpression(context: FunParser.BinaryAdditiveExpressionContext): Any {
        val leftValue = context.additiveExpression().eval().cast<Int>(context)
        val rightValue = context.multiplicativeExpression().eval().cast<Int>(context)
        return when (context.op.type) {
            FunParser.PLUS -> leftValue + rightValue
            FunParser.MINUS -> leftValue - rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryMultiplicativeExpression(context: FunParser.BinaryMultiplicativeExpressionContext): Any {
        val leftValue = context.multiplicativeExpression().eval().cast<Int>(context)
        val rightValue = context.unaryExpression().eval().cast<Int>(context)
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
                context.functionCall() != null -> context.functionCall().eval()
                context.identifier() != null ->
                    getCurrentScope().getVariableValue(context.identifier().eval() as String)
                context.literal() != null -> context.literal().eval()
                context.expression() != null -> context.expression().eval()
                else -> throw getException("Unknown operation type found", context)
            }
        } catch (exception: ScopeException) {
            throw getException(exception.error, context)
        }
    }

    override fun visitFunctionCall(context: FunParser.FunctionCallContext): Any {
        return try {
            val function = getCurrentScope().getFunction(context.identifier().text)
            function(context.arguments().expression().map { it.eval().cast<Int>(context) })
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

    private inline fun <reified T> Any.cast(context: ParserRuleContext): T {
        return this as? T ?: throw getException("Can't evaluate expression value as desired class", context)
    }

    private fun ParserRuleContext.eval(scope: Scope = Scope(scopes.last())): Any {
        scopes.add(scope)
        val value = accept(this@EvalVisitor)
        scopes.removeAt(scopes.size - 1)
        return value
    }

    private fun getCurrentScope() = scopes.last()

    private fun getException(error: String, context: ParserRuleContext) =
            InterpretationException(context.getStart().line, error)
}