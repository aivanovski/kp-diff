package com.github.ai.kpdiff.domain.argument

enum class OptionalArgument(
    shortName: String,
    fullName: String
) {
    // Options to add:
    // --no-color
    // --key-file-a ...
    // --key-file-b ...
    // --key-file ...
    // --verbose
    // --version
    // --flatten

    HELP(shortName = "h", fullName = "help"),
    ONE_PASSWORD(shortName = "o", fullName = "one-password");

    val cliShortName: String = "-$shortName"
    val cliFullName: String = "--$fullName"
}