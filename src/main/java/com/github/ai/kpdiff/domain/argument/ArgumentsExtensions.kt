package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.MutableArguments
import com.github.ai.kpdiff.utils.StringUtils

fun MutableArguments.toArguments(): Arguments {
    return Arguments(
        leftPath = leftPath ?: StringUtils.EMPTY,
        rightPath = rightPath ?: StringUtils.EMPTY,
        keyPath = keyPath,
        leftKeyPath = leftKeyPath,
        rightKeyPath = rightKeyPath,
        differType = differType,
        isUseOnePassword = isUseOnePassword,
        isNoColoredOutput = isNoColoredOutput,
        isPrintHelp = isPrintHelp,
        isPrintVersion = isPrintVersion,
        isVerboseOutput = isVerboseOutput
    )
}