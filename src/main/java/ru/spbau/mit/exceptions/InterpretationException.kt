package ru.spbau.mit.exceptions

class InterpretationException(lineNumber: Int, error: String): RuntimeException("Error on line $lineNumber : $error")