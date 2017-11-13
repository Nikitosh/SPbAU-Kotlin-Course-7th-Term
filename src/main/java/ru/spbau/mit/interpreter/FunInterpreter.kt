package ru.spbau.mit.interpreter

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.OutputStreamWriter

class FunInterpreter(private val fileName: String) {
    fun execute() {
        val lexer = FunLexer(CharStreams.fromFileName(fileName))
        val parser = FunParser(BufferedTokenStream(lexer))
        val evalVisitor = EvalVisitor(OutputStreamWriter(System.out))
        evalVisitor.visit(parser.file())
    }
}