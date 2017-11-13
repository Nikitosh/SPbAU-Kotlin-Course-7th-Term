package ru.spbau.mit.exceptions

class InterpretationException(lineNumber: Int, error: String): Exception("Error on line $lineNumber : $error")