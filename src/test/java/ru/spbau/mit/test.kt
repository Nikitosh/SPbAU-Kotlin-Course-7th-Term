package ru.spbau.mit
import org.junit.Test
import kotlin.test.assertEquals

class TestSource {
    @Test
    fun testDocumentClass() {
        assertEquals("""
            |\documentclass[12pt, a4paper]{article}
            |\begin{document}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article", "12pt", "a4paper")
                }.toString()
        )
    }

    @Test(expected = TexBuilderException::class)
    fun testNoDocumentClass() {
        document {
            usepackage("babel", "russian")
            +"Text"
        }.toString()
    }

    @Test(expected = TexBuilderException::class)
    fun testMoreThanOneDocumentClass() {
        document {
            documentClass("article")
            documentClass("beamer")
            usepackage("babel", "russian")
            +"Text"
        }.toString()
    }

    @Test
    fun testUsepackage() {
        assertEquals("""
            |\documentclass{article}
            |\usepackage[russian, english]{babel}
            |\usepackage[left=15mm, right=15mm, top=30mm, bottom=20mm]{geometry}
            |\usepackage{amsmath}
            |\begin{document}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    usepackage("babel", "russian", "english")
                    usepackage("geometry", "left=15mm", "right=15mm", "top=30mm", "bottom=20mm")
                    usepackage("amsmath")
                }.toString()
        )
    }

    @Test
    fun testFrame() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{frame}
            |\frametitle{title}
            |text
            |\end{frame}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    frame {
                        frameTitle("title")
                        +"text"
                    }
                }.toString()
        )
    }

    @Test
    fun testItemize() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{itemize}[noitemsep]
            |\item
            |text1
            |\item
            |text2
            |\end{itemize}
            |text3
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    itemize("noitemsep") {
                        item {
                            +"text1"
                        }
                        item {
                            +"text2"
                        }
                    }
                    +"text3"
                }.toString()
        )
    }

    @Test
    fun testEnumerate() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{enumerate}[I]
            |\item
            |text1
            |\item
            |text2
            |\end{enumerate}
            |text3
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    enumerate("I") {
                        item {
                            +"text1"
                        }
                        item {
                            +"text2"
                        }
                    }
                    +"text3"
                }.toString()
        )
    }

    @Test
    fun testMath() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{displaymath}
            |a+b
            |\end{displaymath}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    math {
                        +"a+b"
                    }
                }.toString()
        )
    }

    @Test
    fun testAlignment() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{flushleft}
            |text1
            |\end{flushleft}
            |\begin{center}
            |text2
            |\end{center}
            |\begin{flushright}
            |text3
            |\end{flushright}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    left {
                        +"text1"
                    }
                    center {
                        +"text2"
                    }
                    right {
                        +"text3"
                    }
                }.toString()
        )
    }

    @Test
    fun testCustomTag() {
        assertEquals("""
            |\documentclass{article}
            |\begin{document}
            |\begin{customtag}{arg1=value1}{arg2=value2}[option=value3]
            |text
            |\end{customtag}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("article")
                    customCommand("customtag", listOf("arg1=value1", "arg2=value2"), "option=value3") {
                        +"text"
                    }
                }.toString()
        )
    }

    @Test
    fun testSample() {
        assertEquals("""
            |\documentclass{beamer}
            |\usepackage[russian]{babel}
            |\begin{document}
            |\begin{frame}[arg1=arg2]
            |\frametitle{frametitle}
            |\begin{itemize}
            |\item
            |1 text
            |\item
            |2 text
            |\item
            |3 text
            |\end{itemize}
            |\begin{pyglist}[language=kotlin]
            |val a = 1
            |
            |\end{pyglist}
            |\end{frame}
            |\end{document}
            |""".trimMargin(),
                document {
                    documentClass("beamer")
                    usepackage("babel", "russian")
                    frame("arg1=arg2") {
                        frameTitle("frametitle")
                        itemize {
                            for (row in listOf("1", "2", "3")) {
                                item { +"$row text" }
                            }
                        }

                        customCommand("pyglist", listOf(), "language=kotlin") {
                            +"""
                        |val a = 1
                        |
                    """.trimMargin()
                        }
                    }
                }.toString()
        )
    }
}
