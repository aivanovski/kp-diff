package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.output.OutputPrinter

class PrintHelpUseCase(
    private val getVersionUseCase: GetVersionUseCase
) {

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
                -k, --key-file                   Path to key file for <FILE-A> and <FILE-B>
                -a, --key-file-a                 Path to key file for <FILE-A>
                -b, --key-file-b                 Path to key file for <FILE-B>
                -v, --verbose                    Print verbose output (entry fields will be printed)
                -V, --version                    Print version
                -h, --help                       Print help information
        """.trimIndent()
    }
}