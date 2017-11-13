package ru.spbau.mit.exceptions

class InterpretationException(
        val lineNumber: Int,
        val error: String): Exception("Error on line $lineNumber : $error")