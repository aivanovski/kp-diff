package com.github.ai.kpdiff.domain.argument

enum class OptionalArgument(
    val shortName: String,
    val fullName: String
) {
    // Options to add:
    // --verbose
    // --version
    // --flatten

    HELP(shortName = "h", fullName = "help"),
    NO_COLOR(shortName = "n", fullName = "no-color"),
    ONE_PASSWORD(shortName = "o", fullName = "one-password"),
    KEY_FILE(shortName = "k", fullName = "key-file"),
    KEY_FILE_A(shortName = "a", fullName = "key-file-a"),
    KEY_FILE_B(shortName = "b", fullName = "key-file-b");

    val cliShortName: String = "-$shortName"
    val cliFullName: String = "--$fullName"
}