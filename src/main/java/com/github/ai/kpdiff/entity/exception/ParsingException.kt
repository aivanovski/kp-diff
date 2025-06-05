package com.github.ai.kpdiff.entity.exception

open class ParsingException(message: String) : KpDiffException(message)

class InvalidLineFormat(
    line: String
) : ParsingException(
    message = "Invalid line format: $line"
)