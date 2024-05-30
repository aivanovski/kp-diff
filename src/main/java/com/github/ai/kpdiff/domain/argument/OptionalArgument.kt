package com.github.ai.kpdiff.domain.argument

enum class OptionalArgument(
    private val shortName: String?,
    private val fullName: String
) {
    // Options to add:
    // --flatten

    HELP(shortName = "h", fullName = "help"),
    VERSION(shortName = "V", fullName = "version"),
    NO_COLOR(shortName = "n", fullName = "no-color"),
    ONE_PASSWORD(shortName = "o", fullName = "one-password"),
    KEY_FILE(shortName = "k", fullName = "key-file"),
    KEY_FILE_A(shortName = "a", fullName = "key-file-a"),
    KEY_FILE_B(shortName = "b", fullName = "key-file-b"),
    PASSWORD(shortName = "p", fullName = "password"),
    PASSWORD_A(shortName = null, fullName = "password-a"),
    PASSWORD_B(shortName = null, fullName = "password-b"),
    VERBOSE(shortName = "v", fullName = "verbose"),
    DIFF_BY(shortName = "d", fullName = "diff-by"),
    OUTPUT_FILE(shortName = "f", fullName = "output-file");

    val cliShortName: String? = if (shortName != null) {
        "-$shortName"
    } else {
        null
    }

    val cliFullName: String = "--$fullName"
}