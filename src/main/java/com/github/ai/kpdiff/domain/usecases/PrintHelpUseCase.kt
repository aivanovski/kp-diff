package com.github.ai.kpdiff.domain.usecases

class PrintHelpUseCase {

    fun shouldPrintHelp(args: Array<String>): Boolean {
        return args.isEmpty() ||
            args.contains("-h") ||
            args.contains("--help")
    }
}