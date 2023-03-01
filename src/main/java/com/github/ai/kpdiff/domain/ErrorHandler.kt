package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.output.OutputWriter
import com.github.ai.kpdiff.entity.Either

class ErrorHandler(
    private val writer: OutputWriter
) {

    fun handleIfLeft(result: Either<*>) {
        if (result.isLeft()) {
            writer.printLine(
                String.format(
                    Errors.ERROR_HAS_BEEN_OCCURRED,
                    result.unwrapError().toString()
                )
            )
            writer.printStackTrace(result.unwrapError())
        }
    }
}