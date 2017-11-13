package ru.spbau.mit

import ru.spbau.mit.exceptions.InterpretationException
import ru.spbau.mit.interpreter.FunInterpreter
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Error: expected one argument with file name");
    } else {
        val interpreter = FunInterpreter(args[0])
        try {
            interpreter.execute()
        } catch (exception: FileNotFoundException) {
            System.err.println("Error: file not found");
        } catch (exception: InterpretationException) {
            System.err.println(exception.message)
        }
    }
}