package ru.spbau.mit

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.CharStreams
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.File
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun testPriorities() {
        val parser = FunParser(BufferedTokenStream(FunLexer(CharStreams.fromString("1+2*3>5"))))
        val expectedParseTree =
"""File
  Block
    Statement
      Expression
        UnaryOrExpression
          UnaryAndExpression
            UnaryEqualityExpression
              BinaryRelationalExpression
                UnaryRelationalExpression
                  BinaryAdditiveExpression
                    UnaryAdditiveExpression
                      UnaryMultiplicativeExpression
                        UnaryExpression
                          Literal
                            [1]
                    [+]
                    BinaryMultiplicativeExpression
                      UnaryMultiplicativeExpression
                        UnaryExpression
                          Literal
                            [2]
                      [*]
                      UnaryExpression
                        Literal
                          [3]
                [>]
                UnaryAdditiveExpression
                  UnaryMultiplicativeExpression
                    UnaryExpression
                      Literal
                        [5]
"""
        assertEquals(expectedParseTree, toParseTree(parser.file()))
    }

    @Test
    fun testExample1() {
        val parser = FunParser(BufferedTokenStream(FunLexer(CharStreams.fromFileName(EXAMPLE1_FILE_NAME))))
        assertEquals(File(EXAMPLE1_PARSE_TREE_FILE_NAME).readText(), toParseTree(parser.file()))
    }

    @Test
    fun testExample2() {
        val parser = FunParser(BufferedTokenStream(FunLexer(CharStreams.fromFileName(EXAMPLE2_FILE_NAME))))
        assertEquals(File(EXAMPLE2_PARSE_TREE_FILE_NAME).readText(), toParseTree(parser.file()))
    }

    @Test
    fun testExample3() {
        val parser = FunParser(BufferedTokenStream(FunLexer(CharStreams.fromFileName(EXAMPLE3_FILE_NAME))))
        assertEquals(File(EXAMPLE3_PARSE_TREE_FILE_NAME).readText(), toParseTree(parser.file()))
    }
}