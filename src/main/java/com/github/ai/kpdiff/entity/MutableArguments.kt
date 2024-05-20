package com.github.ai.kpdiff.entity

data class MutableArguments(
    var leftPath: String? = null,
    var rightPath: String? = null,
    var keyPath: String? = null,
    var leftKeyPath: String? = null,
    var rightKeyPath: String? = null,
    var password: String? = null,
    var leftPassword: String? = null,
    var rightPassword: String? = null,
    var differType: DifferType? = null,
    var outputFilePath: String? = null,
    var isUseOnePassword: Boolean = false,
    var isNoColoredOutput: Boolean = false,
    var isPrintHelp: Boolean = false,
    var isPrintVersion: Boolean = false,
    var isVerboseOutput: Boolean = false
)