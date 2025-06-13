package com.github.ai.kpdiff.entity.exception

import com.github.ai.kpdiff.domain.patch.model.TextLine

open class ParsingException(message: String) : KpDiffException(message)

open class PatchParsingException(
    message: String
) : ParsingException(message = message)

class InvalidPatchFileFormat : PatchParsingException(
    message = "Invalid .patch file format"
)

class InvalidPatchLineFormat(
    line: TextLine
) : PatchParsingException(
    message = "Invalid line format at ${line.number}: ${line.text}"
)

class InvalidValueFormat : PatchParsingException(
    message = "Invalid value format"
)