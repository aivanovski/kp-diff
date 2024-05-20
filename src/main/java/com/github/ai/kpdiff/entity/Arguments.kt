package com.github.ai.kpdiff.entity

import com.github.ai.kpdiff.utils.StringUtils

data class Arguments(
    val leftPath: String,
    val rightPath: String,
    val keyPath: String?,
    val leftKeyPath: String?,
    val rightKeyPath: String?,
    val password: String?,
    val leftPassword: String?,
    val rightPassword: String?,
    val differType: DifferType?,
    val outputFilePath: String?,
    val isUseOnePassword: Boolean,
    val isNoColoredOutput: Boolean,
    val isPrintHelp: Boolean,
    val isPrintVersion: Boolean,
    val isVerboseOutput: Boolean
) {

    companion object {
        val EMPTY_ARGUMENTS = Arguments(
            leftPath = StringUtils.EMPTY,
            rightPath = StringUtils.EMPTY,
            keyPath = null,
            leftKeyPath = null,
            rightKeyPath = null,
            password = null,
            leftPassword = null,
            rightPassword = null,
            differType = null,
            outputFilePath = null,
            isUseOnePassword = false,
            isNoColoredOutput = false,
            isPrintHelp = false,
            isPrintVersion = false,
            isVerboseOutput = false
        )
    }
}