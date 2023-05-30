package com.github.ai.kpdiff

import com.github.ai.kpdiff.testEntities.TestKeepassDatabase
import com.github.ai.kpdiff.testEntities.TestKeepassEntry
import com.github.ai.kpdiff.testEntities.TestKeepassGroup
import com.github.ai.kpdiff.testEntities.TestKeepassKey
import java.util.UUID

object TestData {

    const val VERSION = "4.5.6"
    const val FILE_PATH = "/path/to/file.kdbx"
    const val FILE_NAME = "file.kdbx"
    const val LEFT_FILE_PATH = "/path/to/left.kdbx"
    const val RIGHT_FILE_PATH = "/path/to/right.kdbx"
    const val KEY_PATH = "/path/to/key.key"
    const val LEFT_KEY_PATH = "/path/to/left/key.key"
    const val RIGHT_KEY_PATH = "/path/to/right/key.key"
    const val PASSWORD = "test-password"
    const val LEFT_PASSWORD = "left-password"
    const val RIGHT_PASSWORD = "right-password"
    const val INVALID_PASSWORD = "invalid-password"
    const val FILE_CONTENT = "mocked-file-content"
    const val EXCEPTION_MESSAGE = "Exception message"
    const val NAME = "field-name"
    const val TITLE = "entry-title"
    const val VALUE = "value"
    const val VALUE1 = "field-value1"
    const val VALUE2 = "field-value2"
    const val TITLE1 = "entry-title1"
    const val TITLE2 = "entry-title2"
    val UUID1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val UUID_CHILD = UUID.fromString("00000000-0000-0000-0000-100000000001")
    val UUID_PARENT = UUID.fromString("00000000-0000-0000-0000-100000000002")

    const val INDENT_EMPTY = ""
    val INDENT_SINGLE = ".".repeat(4)
    val INDENT_DOUBLE = ".".repeat(8)

    val DB_WITH_KEY = TestKeepassDatabase(
        key = TestKeepassKey.FileKey("db-key"),
        filename = "db-with-key.kdbx",
        root = TestKeepassGroup(
            uuid = UUID.fromString("c285cc88-6eb7-473e-a91a-1715e8689f79"),
            name = "Root",
            entries = listOf(
                TestKeepassEntry(
                    uuid = UUID.fromString("15077a66-c0f0-4a71-9352-fed3b1c01f37"),
                    title = "Entry 1",
                    username = "scott",
                    password = "tiger"
                )
            ),
            groups = listOf(
                TestKeepassGroup(
                    uuid = UUID.fromString("a5f9fa21-73cf-4da8-9c5c-39f8dd61e9c2"),
                    name = "Root group 1",
                    entries = listOf(
                        TestKeepassEntry(
                            uuid = UUID.fromString("05d00bab-9544-4636-aece-ff851561c5e3"),
                            title = "Entry 2",
                            username = "scott",
                            password = "tiger"
                        )
                    ),
                    groups = listOf(
                        TestKeepassGroup(
                            uuid = UUID.fromString("f7adcc56-92da-4ac9-b72e-441a3add52ca"),
                            name = "Inner group 1",
                            entries = listOf(
                                TestKeepassEntry(
                                    uuid = UUID.fromString("0484d3b6-2d0d-426e-bf5a-3eee7ebf0985"),
                                    title = "Entry 3",
                                    username = "scott",
                                    password = "tiger"
                                ),
                                TestKeepassEntry(
                                    uuid = UUID.fromString("a97f8961-018e-411c-be10-59a4b6777433"),
                                    title = "Entry 4",
                                    username = "scott",
                                    password = "tiger"
                                )
                            ),
                            groups = emptyList()
                        ),
                        TestKeepassGroup(
                            uuid = UUID.fromString("27e9ad5d-b428-40d4-b7dd-0be9fe5cfde7"),
                            name = "Inner group 2",
                            entries = emptyList(),
                            groups = emptyList()
                        )
                    )
                )
            )
        )
    )

    val DB_WITH_PASSWORD = TestKeepassDatabase(
        key = TestKeepassKey.PasswordKey("abc123"),
        filename = "db-with-password.kdbx",
        root = TestKeepassGroup(
            uuid = UUID.fromString("6a09a1cd-b39e-5564-f736-a9fd0993bd80"),
            name = "Root",
            entries = listOf(
                TestKeepassEntry(
                    uuid = UUID.fromString("15077a66-c0f0-4a71-9352-fed3b1c01f37"),
                    title = "Entry 1",
                    username = "scott",
                    password = "tiger"
                )
            ),
            groups = listOf(
                TestKeepassGroup(
                    uuid = UUID.fromString("a5f9fa21-73cf-4da8-9c5c-39f8dd61e9c2"),
                    name = "Root group 1",
                    entries = listOf(
                        TestKeepassEntry(
                            uuid = UUID.fromString("05d00bab-9544-4636-aece-ff851561c5e3"),
                            title = "Entry 2",
                            username = "scott",
                            password = "tiger"
                        )
                    ),
                    groups = listOf(
                        TestKeepassGroup(
                            uuid = UUID.fromString("f7adcc56-92da-4ac9-b72e-441a3add52ca"),
                            name = "Inner group 1",
                            entries = listOf(
                                TestKeepassEntry(
                                    uuid = UUID.fromString("0484d3b6-2d0d-426e-bf5a-3eee7ebf0985"),
                                    title = "Entry 3",
                                    username = "scott",
                                    password = "tiger"
                                ),
                                TestKeepassEntry(
                                    uuid = UUID.fromString("a97f8961-018e-411c-be10-59a4b6777433"),
                                    title = "Entry 4",
                                    username = "scott",
                                    password = "tiger"
                                )
                            ),
                            groups = emptyList()
                        ),
                        TestKeepassGroup(
                            uuid = UUID.fromString("27e9ad5d-b428-40d4-b7dd-0be9fe5cfde7"),
                            name = "Inner group 2",
                            entries = emptyList(),
                            groups = emptyList()
                        )
                    )
                )
            )
        )
    )

    val DB_WITH_PASSWORD_MODIFIED = TestKeepassDatabase(
        key = TestKeepassKey.PasswordKey("abc123"),
        filename = "db-with-password-modified.kdbx",
        root = TestKeepassGroup(
            uuid = UUID.fromString("6a09a1cd-b39e-5564-f736-a9fd0993bd80"),
            name = "Root",
            entries = listOf(
                TestKeepassEntry(
                    uuid = UUID.fromString("15077a66-c0f0-4a71-9352-fed3b1c01f37"),
                    title = "Entry 1",
                    username = "scott",
                    password = "tiger"
                ),
                TestKeepassEntry(
                    uuid = UUID.fromString("a63d4446-b465-483b-b955-3a54b22790ac"),
                    title = "Entry 5",
                    username = "scott",
                    password = "tiger"
                )
            ),
            groups = listOf(
                TestKeepassGroup(
                    uuid = UUID.fromString("a5f9fa21-73cf-4da8-9c5c-39f8dd61e9c2"),
                    name = "Root group 1",
                    entries = listOf(
                        TestKeepassEntry(
                            uuid = UUID.fromString("05d00bab-9544-4636-aece-ff851561c5e3"),
                            title = "Entry 2",
                            username = "scott",
                            password = "tiger"
                        )
                    ),
                    groups = listOf(
                        TestKeepassGroup(
                            uuid = UUID.fromString("f7adcc56-92da-4ac9-b72e-441a3add52ca"),
                            name = "Inner group 1",
                            entries = listOf(
                                TestKeepassEntry(
                                    uuid = UUID.fromString("a97f8961-018e-411c-be10-59a4b6777433"),
                                    title = "Entry 4 modified",
                                    username = "scott",
                                    password = "tiger"
                                )
                            ),
                            groups = emptyList()
                        ),
                        TestKeepassGroup(
                            uuid = UUID.fromString("4ee53b54-0778-4327-a51f-c82a117103a9"),
                            name = "Inner group 3",
                            entries = emptyList(),
                            groups = emptyList()
                        )
                    )
                )
            )
        )
    )
}