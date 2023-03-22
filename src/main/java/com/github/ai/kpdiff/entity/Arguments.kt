package com.github.ai.kpdiff.entity

data class Arguments(
    val leftPath: String,
    val rightPath: String,
    val isUseOnePassword: Boolean,
    val isNoColoredOutput: Boolean,
    val keyPath: String?,
    val leftKeyPath: String?,
    val rightKeyPath: String?
)