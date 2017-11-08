package ru.spbau.mit

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

const val EXAMPLE1_FILE_NAME = "src/test/resources/example1.fun"
const val EXAMPLE2_FILE_NAME = "src/test/resources/example2.fun"
const val EXAMPLE3_FILE_NAME = "src/test/resources/example3.fun"

const val EXAMPLE1_PARSE_TREE_FILE_NAME = "src/test/resources/example1ParseTree.txt"
const val EXAMPLE2_PARSE_TREE_FILE_NAME = "src/test/resources/example2ParseTree.txt"
const val EXAMPLE3_PARSE_TREE_FILE_NAME = "src/test/resources/example3ParseTree.txt"

fun toParseTree(context: ParserRuleContext, indentation: String = "") : String {
    val stringBuilder = StringBuilder()
    val name = context.javaClass.simpleName.removeSuffix("Context")
    stringBuilder.append("$indentation$name\n")
    context.children.forEach { child ->
        when (child) {
            is ParserRuleContext -> stringBuilder.append(toParseTree(child, indentation + "  "))
            is TerminalNode -> stringBuilder.append("$indentation  [${child.text}]\n")
        }
    }
    return stringBuilder.toString()
}