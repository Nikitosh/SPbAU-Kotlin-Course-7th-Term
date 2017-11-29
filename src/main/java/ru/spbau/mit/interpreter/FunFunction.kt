package ru.spbau.mit.interpreter

import ru.spbau.mit.parser.FunParser

class FunFunction(
        private val parameterNames: List<String>,
        private val body: FunParser.BlockWithBracesContext
): Function{
    override fun apply(visitor: EvalVisitor, arguments: List<Int>): Int {
        if (arguments.size != parameterNames.size) {
            throw visitor.getException("Illegal number of arguments for function", body)
        }
        val scope = Scope(visitor.getCurrentScope())
        for (i in 0 until arguments.size) {
            scope.defineVariable(parameterNames[i])
            scope.setVariableValue(parameterNames[i], arguments[i])
        }
        visitor.addScope(scope)
        val value = visitor.visit(body)
        visitor.removeScope()
        return if (value is Unit) 0 else value as Int
    }
}