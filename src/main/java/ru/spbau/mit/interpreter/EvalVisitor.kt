package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.ParserRuleContext
import ru.spbau.mit.exceptions.InterpretationException
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
        val statements = context.statement()
        for (statement in statements) {
            val value = visit(statement)
            if (value != Unit &&
                    (statement.ifStatement() != null
                            || statement.returnStatement() != null
                            || statement.whileStatement() != null)) {
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
        val variableName = visit(context.identifier()) as String
        getCurrentScope().defineVariable(variableName)
        context.expression()?.let {
            getCurrentScope().setVariableValue(variableName, visit(it) as Int)
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
        val variableName = visit(context.identifier()) as String
        val value = visit(context.expression()) as Int
        getCurrentScope().setVariableValue(variableName, value)
    }

    override fun visitReturnStatement(context: FunParser.ReturnStatementContext): Any {
        return visit(context.expression())
    }

    override fun visitLogicalExpression(context: FunParser.LogicalExpressionContext): Any {
        val leftValue = visit(context.expression(0)) as Boolean
        val rightValue = visit(context.expression(1)) as Boolean
        return when (context.op.type) {
            FunParser.AND -> leftValue && rightValue
            FunParser.OR -> leftValue || rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitBinaryExpression(context: FunParser.BinaryExpressionContext): Any {
        val leftValue = visit(context.expression(0)) as Int
        val rightValue = visit(context.expression(1)) as Int
        return when (context.op.type) {
            FunParser.MUL -> leftValue * rightValue
            FunParser.DIV -> leftValue / rightValue
            FunParser.MOD -> leftValue % rightValue
            FunParser.PLUS -> leftValue + rightValue
            FunParser.MINUS -> leftValue - rightValue
            FunParser.GT-> leftValue > rightValue
            FunParser.LT -> leftValue < rightValue
            FunParser.GE -> leftValue >= rightValue
            FunParser.LE -> leftValue <= rightValue
            FunParser.EQ -> leftValue == rightValue
            FunParser.NEQ -> leftValue != rightValue
            else -> throw getException("Unknown operation type found", context)
        }
    }

    override fun visitIdentifierExpression(context: FunParser.IdentifierExpressionContext): Any {
        return getCurrentScope().getVariableValue(visit(context.identifier()) as String)
    }

    override fun visitFunctionCall(context: FunParser.FunctionCallContext): Any {
        val functionName = context.identifier().text
        val function = getCurrentScope().getFunction(functionName)
        val arguments = context.arguments().expression().map { visit(it) as Int }
        return function.apply(this, arguments)
    }

    override fun visitIdentifier(context: FunParser.IdentifierContext): Any {
        return context.Identifier().text;
    }

    override fun visitLiteral(context: FunParser.LiteralContext): Any {
        return context.Number().text.toInt()
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