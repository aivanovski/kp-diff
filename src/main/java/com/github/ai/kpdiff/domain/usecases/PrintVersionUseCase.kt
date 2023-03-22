package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.domain.argument.OptionalArgument
import com.github.ai.kpdiff.domain.output.OutputPrinter

class PrintVersionUseCase(
    private val getVersionUseCase: GetVersionUseCase
) {

    fun shouldPrintVersion(args: Array<String>): Boolean {
        return args.contains(OptionalArgument.VERSION.cliShortName) ||
            args.contains(OptionalArgument.VERSION.cliFullName)
    }

    fun printVersion(printer: OutputPrinter) {
        val version = getVersionUseCase.getVersionName()
        printer.printLine(String.format(TEXT, version))
    }

    companion object {
        internal const val TEXT = "kp-diff %s"
    }
}