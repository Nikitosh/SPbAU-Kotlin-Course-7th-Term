package ru.spbau.mit.interpreter

import ru.spbau.mit.exceptions.ScopeException

class Scope(private val parentScope: Scope? = null) {
    private val variables: MutableMap<String, Int> = mutableMapOf()
    private val functions: MutableMap<String, Function> = mutableMapOf()

    fun defineVariable(variableName: String) = variables.define(variableName, 0)

    fun getVariableValue(variableName: String): Int = variables[variableName]
            ?: parentScope?.getVariableValue(variableName).mustBeNonNull("Variable $variableName is not defined")

    fun setVariableValue(variableName: String, value: Int) {
        if (variableName in variables) {
            variables[variableName] = value
        } else {
            parentScope?.setVariableValue(variableName, value).mustBeNonNull("Variable $variableName is not defined")
        }
    }

    fun defineFunction(functionName: String, function: Function) = functions.define(functionName, function)

    fun getFunction(functionName: String): Function = functions[functionName]
            ?: parentScope?.getFunction(functionName).mustBeNonNull("Function $functionName is not defined")

    private fun <T> MutableMap<String, T>.define(name: String, value: T) {
        check(name !in this, "$name is already defined in this scope")
        this[name] = value
    }

    private fun <T> T?.mustBeNonNull(message: String): T {
        return this ?: throw ScopeException(message)
    }

    private fun check(value: Boolean, message: String) {
        if (!value) {
            throw ScopeException(message)
        }
    }
}