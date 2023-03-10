package com.github.ai.kpdiff.domain.argument

enum class OptionalArgument(
    shortName: String,
    fullName: String
) {
    HELP(shortName = "h", fullName = "help");

    val cliShortName: String = "-$shortName"
    val cliFullName: String = "--$fullName"
}