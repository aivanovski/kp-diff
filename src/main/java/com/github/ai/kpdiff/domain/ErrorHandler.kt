package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either

class ErrorHandler(
    private val writer: OutputPrinter
) {

    fun handleIfLeft(result: Either<*>) {
        if (result.isLeft()) {
            val message = result.unwrapError().message

            if (!message.isNullOrEmpty()) {
                writer.printLine(
                    String.format(
                        Strings.ERROR_HAS_BEEN_OCCURRED,
                        message
                    )
                )
            } else {
                writer.printStackTrace(result.unwrapError())
            }
        }
    }
}