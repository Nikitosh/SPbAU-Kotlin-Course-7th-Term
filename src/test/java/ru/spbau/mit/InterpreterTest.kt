package ru.spbau.mit

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.junit.Test
import ru.spbau.mit.exceptions.InterpretationException
import ru.spbau.mit.interpreter.EvalVisitor
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.StringWriter
import kotlin.test.assertEquals

class InterpreterTest {
    @Test
    fun testExpressionEvaluation() {
        assertEquals("46\n", evaluate(CharStreams.fromString("var b=5\nprintln(1+5*10-b)")))
    }

    @Test(expected = InterpretationException::class)
    fun testException1() {
        evaluate(CharStreams.fromString("var b=5\nprintln(a)"))
    }

    @Test(expected = InterpretationException::class)
    fun testException2() {
        evaluate(CharStreams.fromString("fun foo(n) {\nprintln(n)\n}\nfoo(5, 10)\n"))
    }

    @Test(expected = InterpretationException::class)
    fun testException3() {
        evaluate(CharStreams.fromString("var a = 001"))
    }

    @Test(expected = InterpretationException::class)
    fun testException4() {
        evaluate(CharStreams.fromString("var 2a = 5"))
    }

    @Test
    fun testSmallProgram() {
        assertEquals("1 2\n2 4\n3 10\n4 16\n", evaluate(CharStreams.fromString(
"""
fun sq(n) {
    return n * n
}

var i = 1
while (i <= 4) {
    var k = sq(i)
    if (k % 2 == 0) {
        println(i, k)
    } else {
        println(i, i * i + 1)
    }
    i = i + 1
}
""")))
    }

    @Test
    fun testExample1() {
        assertEquals("0\n", evaluate(CharStreams.fromFileName(EXAMPLE1_FILE_NAME)))
    }

    @Test
    fun testExample2() {
        assertEquals("1 1\n2 2\n3 3\n4 5\n5 8\n", evaluate(CharStreams.fromFileName(EXAMPLE2_FILE_NAME)))
    }

    @Test
    fun testExample3() {
        assertEquals("42\n", evaluate(CharStreams.fromFileName(EXAMPLE3_FILE_NAME)))
    }

    private fun evaluate(charStream: CharStream): String {
        val parser = FunParser(BufferedTokenStream(FunLexer(charStream)))
        val writer = StringWriter()
        val visitor = EvalVisitor(writer)
        visitor.visit(parser.file())
        return writer.toString()
    }
}