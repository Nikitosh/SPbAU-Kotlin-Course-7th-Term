package ru.spbau.mit

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.OutputStream
import java.nio.file.Files
import java.util.concurrent.TimeUnit

interface Element {
    fun render(builder: StringBuilder)
}

class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder) {
        builder.append("$text\n")
    }
}

@DslMarker
annotation class TexCommandMarker

@TexCommandMarker
abstract class Command(
        val name: String,
        private val arguments: List<String> = listOf(),
        private val options: List<String> = listOf()
) : Element {
    private val children = arrayListOf<Element>()

    protected fun <T : Element> initCommand(command: T, init: T.() -> Unit = {}): T {
        command.init()
        children.add(command)
        return command
    }

    protected fun renderChildren(builder: StringBuilder) {
        children.forEach { it.render(builder) }
    }

    protected fun renderArguments(): String {
        return arguments.joinToString(separator = "", transform = { "{$it}" })
    }

    protected fun renderOptions(): String {
        return if (options.isEmpty()) "" else options.joinToString(prefix = "[", postfix = "]")
    }

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    override fun toString(): String = buildString(this::render)

    fun toOutputStream(outputStream: OutputStream) {
        outputStream.write(toString().toByteArray())
    }
}

abstract class CommandWithoutContent(
        name: String,
        arguments: List<String> = listOf(),
        options: List<String> = listOf()
): Command(name, arguments, options) {
    override fun render(builder: StringBuilder) {
        builder.append("\\$name${renderOptions()}${renderArguments()}\n")
        renderChildren(builder)
    }
}

abstract class BlockCommand(
        name: String,
        arguments: List<String> = listOf(),
        options: List<String> = listOf()
): Command(name, arguments, options) {
    override fun render(builder: StringBuilder) {
        builder.append("\\begin{$name}${renderArguments()}${renderOptions()}\n")
        renderChildren(builder)
        builder.append("\\end{$name}\n")
    }
}

open class CommandWithContent(
        name: String,
        arguments: List<String> = listOf(),
        options: List<String> = listOf()
): BlockCommand(name, arguments, options) {
    fun itemize(vararg options: String, init: Itemize.() -> Unit) = initCommand(Itemize(listOf(*options)), init)

    fun enumerate(vararg options: String, init: Enumerate.() -> Unit) = initCommand(Enumerate(listOf(*options)), init)

    fun math(vararg options: String, init: Math.() -> Unit) = initCommand(Math(listOf(*options)), init)

    fun left(init: CommandWithContent.() -> Unit) = initCommand(CommandWithContent("flushleft"), init)

    fun center(init: CommandWithContent.() -> Unit) = initCommand(CommandWithContent("center"), init)

    fun right(init: CommandWithContent.() -> Unit) = initCommand(CommandWithContent("flushright"), init)

    fun customCommand(
            name: String,
            arguments: List<String>,
            vararg options: String,
            init: CustomCommandWithContent.() -> Unit
    ) = initCommand(CustomCommandWithContent(name, arguments, listOf(*options)), init)
}

abstract class CommandWithItems(
        name: String,
        arguments: List<String> = listOf(),
        options: List<String> = listOf()
): BlockCommand(name, arguments, options) {
    fun item(init: Item.() -> Unit) = initCommand(Item(), init)
}

class CustomCommandWithContent(
        name: String,
        arguments: List<String> = listOf(),
        options: List<String> = listOf()
): CommandWithContent(name, arguments, options)

class Item: CommandWithoutContent("item")

class FrameTitle(frameTitle: String): CommandWithoutContent("frametitle", listOf(frameTitle))

class Frame(options: List<String>): CommandWithContent("frame", listOf(), options) {
    fun frameTitle(frameTitle: String) = initCommand(FrameTitle(frameTitle))
}

class Itemize(options: List<String>): CommandWithItems("itemize", listOf(), options)

class Enumerate(options: List<String>): CommandWithItems("enumerate", listOf(), options)

class Math(options: List<String>): CommandWithContent("displaymath", listOf(), options)

class DocumentClass(
        className: String,
        options: List<String>
): CommandWithoutContent("documentclass", listOf(className), options)

class UsePackage(
        packageName: String,
        options: List<String>
): CommandWithoutContent("usepackage", listOf(packageName), options)

class Document: CommandWithContent("document") {
    private var documentClass: DocumentClass? = null

    private val packages: MutableList<UsePackage> = mutableListOf()

    override fun render(builder: StringBuilder) {
        documentClass!!.render(builder)
        packages.forEach { it.render(builder) }
        super.render(builder)
    }

    fun documentClass(className: String, vararg options: String) {
        require(documentClass == null)
        documentClass = DocumentClass(className, listOf(*options))
    }

    fun usepackage(packageName: String, vararg options: String) {
        packages.add(UsePackage(packageName, listOf(*options)))
    }

    fun frame(vararg options: String, init: Frame.() -> Unit) = initCommand(Frame(listOf(*options)), init)

    fun hasDocumentClass() = documentClass != null

    fun toPdf(filename: String) {
        val latexFilename = "$filename.tex"
        val latexFile = File(latexFilename)
        FileUtils.writeStringToFile(latexFile, toString())
        ProcessBuilder("pdflatex", latexFilename)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.SECONDS)
        for (extension in listOf("aux", "log", "nav", "out", "snm", "toc", "tex")) {
            Files.deleteIfExists(File("$filename.$extension").toPath())
        }
    }
}

fun document(init: Document.() -> Unit): Document = Document().apply(init)
        .let { return if (it.hasDocumentClass()) it else throw TexBuilderException("No document class found") }
