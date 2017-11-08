package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Token
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import kotlin.test.assertEquals

class LexerTest {
    @Test
    fun testIdentifiers() {
        val lexer = FunLexer(CharStreams.fromString("aba_1_aba ___a _    z 10a"))
        val expectedTokens = listOf<String>("aba_1_aba", "___a", "_", "z", "10a")
        assertEquals(expectedTokens, lexer.allTokens.map(Token::getText))
    }

    @Test
    fun testNumbers() {
        val lexer = FunLexer(CharStreams.fromString("0 -250 199 00001"))
        val expectedTokens = listOf<String>("0", "-250", "199", "00001")
        assertEquals(expectedTokens, lexer.allTokens.map(Token::getText))
    }

    @Test
    fun testExample1() {
        val lexer = FunLexer(CharStreams.fromFileName(TestUtils.EXAMPLE1_FILE_NAME))
        val expectedTokens = listOf<String>(
                "var", "a", "=", "10", "var", "b", "=", "20", "if", "(", "a", ">", "b", ")", "{", "println", "(", "1",
                ")", "}", "else", "{", "println", "(", "0", ")", "}"
        )
        assertEquals(expectedTokens, lexer.allTokens.map(Token::getText))
    }

    @Test
    fun testExample2() {
        val lexer = FunLexer(CharStreams.fromFileName(TestUtils.EXAMPLE2_FILE_NAME))
        val expectedTokens = listOf<String>(
                "fun", "fib", "(", "n", ")", "{", "if", "(", "n", "<=", "1", ")", "{", "return", "1", "}", "return",
                "fib", "(", "n", "-", "1", ")", "+", "fib", "(", "n", "-", "2", ")", "}", "var", "i", "=", "1", "while",
                "(", "i", "<=", "5", ")", "{", "println", "(", "i", ",", "fib", "(", "i", ")", ")", "i", "=", "i", "+",
                "1", "}"
        )
        assertEquals(expectedTokens, lexer.allTokens.map(Token::getText))
    }

    @Test
    fun testExample3() {
        val lexer = FunLexer(CharStreams.fromFileName(TestUtils.EXAMPLE3_FILE_NAME))
        val expectedTokens = listOf<String>(
                "fun", "foo", "(", "n", ")", "{", "fun", "bar", "(", "m", ")", "{", "return", "m", "+", "n", "}",
                "return", "bar", "(", "1", ")", "}", "println", "(", "foo", "(", "41", ")", ")"
        )
        assertEquals(expectedTokens, lexer.allTokens.map(Token::getText))
    }
}