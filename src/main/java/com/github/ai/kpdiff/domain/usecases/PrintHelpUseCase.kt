package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.argument.OptionalArgument
import com.github.ai.kpdiff.domain.output.OutputPrinter

class PrintHelpUseCase(
    private val getVersionUseCase: GetVersionUseCase
) {

    fun shouldPrintHelp(args: Array<String>): Boolean {
        return args.isEmpty() ||
            args.contains(OptionalArgument.HELP.cliShortName) ||
            args.contains(OptionalArgument.HELP.cliFullName)
    }

    fun printHelp(printer: OutputPrinter) {
        printer.printLine(
            String.format(
                HELP_TEXT,
                getVersionUseCase.getVersionName()
            )
        )
    }

    companion object {
        internal val HELP_TEXT = """
            kp-diff %s
            Compare Keepass (.kdbx) files and prints differences

            USAGE:
                kp-diff [OPTIONS] <FILE-A> <FILE-B>

            ARGS:
                <FILE-A>    First file
                <FILE-B>    Second file

            OPTIONS:
                -o, --one-password               Use one password for both files
                -h, --help                       Print help information
        """.trimIndent()
    }
}