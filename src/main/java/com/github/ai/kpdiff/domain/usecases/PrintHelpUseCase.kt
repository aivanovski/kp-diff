package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.output.OutputPrinter

class PrintHelpUseCase(
    private val getVersionUseCase: GetVersionUseCase
) {

    fun shouldPrintHelp(args: Array<String>): Boolean {
        return args.isEmpty() ||
            args.contains("-h") ||
            args.contains("--help")
    }

    fun printHelp(printer: OutputPrinter) {
        printer.printLine(
            String.format(
                HELP,
                getVersionUseCase.getVersionName()
            )
        )
    }

    companion object {
        internal val HELP = """
            kp-diff %s
            Compare Keepass (.kdbx) files and prints differences

            USAGE:
                kp-diff [OPTIONS] <FILE-A> <FILE-B>

            ARGS:
                <FILE-A>    First file
                <FILE-B>    Second file

            OPTIONS:
                -h, --help                       Print help information
        """.trimIndent()
    }
}