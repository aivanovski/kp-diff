package com.github.ai.kpdiff.domain.argument

import com.github.ai.kpdiff.entity.DifferType

enum class OptionalArgument(
    val shortName: String,
    val fullName: String,
    val values: List<String> = emptyList(),
    val defaultValue: String? = null
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
    VERBOSE(shortName = "v", fullName = "verbose"),
    DIFF_BY(
        shortName = "d",
        fullName = "diff-by",
        values = listOf(
            DifferType.PATH.cliName,
            DifferType.UUID.cliName
        ),
        defaultValue = DifferType.PATH.cliName
    );

    val cliShortName: String = "-$shortName"
    val cliFullName: String = "--$fullName"
}