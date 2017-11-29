package ru.spbau.mit.interpreter

interface Function {
    fun apply(visitor: EvalVisitor, arguments: List<Int>): Int
}