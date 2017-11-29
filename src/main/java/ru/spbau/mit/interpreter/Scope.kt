package ru.spbau.mit.interpreter

import ru.spbau.mit.exceptions.ScopeException

class Scope(private val parentScope: Scope? = null) {
    private val variables: MutableMap<String, Int> = mutableMapOf()
    private val functions: MutableMap<String, Function> = mutableMapOf()

    fun defineVariable(variableName: String) {
        if (variableName in variables) {
            throw ScopeException("Variable $variableName is already defined in this scope")
        }
        variables[variableName] = 0
    }

    fun getVariableValue(variableName: String): Int = variables[variableName]
            ?: parentScope?.getVariableValue(variableName)
            ?: throw ScopeException("Variable $variableName is not defined")

    fun setVariableValue(variableName: String, value: Int) {
        if (variableName in variables) {
            variables[variableName] = value
        } else {
            parentScope?.setVariableValue(variableName, value)
                    ?: throw ScopeException("Variable $variableName is not defined")
        }
    }

    fun defineFunction(functionName: String, function: Function) {
        if (functionName in functions) {
            throw ScopeException("Function $functionName is already defined in this scope")
        }
        functions[functionName] = function
    }

    fun getFunction(functionName: String): Function = functions[functionName]
            ?: parentScope?.getFunction(functionName)
            ?: throw ScopeException("Function $functionName is not defined")
}