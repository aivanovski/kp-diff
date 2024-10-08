package com.github.ai.kpdiff

import com.github.ai.kpdiff.TestEntityFactory.newBinary
import com.github.ai.kpdiff.TestEntityFactory.newEntry
import com.github.ai.kpdiff.TestEntityFactory.newGroup
import com.github.ai.kpdiff.utils.Fields
import java.util.UUID

object TestData {

    const val INVALID = "invalid-value"
    const val VERSION = "4.5.6"
    const val OUTPUT_FILE_PATH = "/path/to/output-file.patch"
    const val FILE_PATH = "/path/to/file.kdbx"
    const val PARENT_PATH = "/path/to"
    const val FILE_NAME = "file.kdbx"
    const val LEFT_FILE_PATH = "/path/to/left.kdbx"
    const val RIGHT_FILE_PATH = "/path/to/right.kdbx"
    const val PATCH_FILE_PATH = "/path/to/patch-file'"
    const val KEY_PATH = "/path/to/key.key"
    const val LEFT_KEY_PATH = "/path/to/left/key.key"
    const val RIGHT_KEY_PATH = "/path/to/right/key.key"
    const val PASSWORD = "test-password"
    const val LEFT_PASSWORD = "left-password"
    const val RIGHT_PASSWORD = "right-password"
    const val INVALID_PASSWORD = "invalid-password"
    const val FILE_CONTENT = "mocked-file-content"
    const val UPDATED_FILE_CONTENT = "updated-file-content"
    const val EXCEPTION_MESSAGE = "Exception message"
    const val NAME = "field-name"
    const val TITLE = "entry-title"
    const val VALUE = "value"
    const val VALUE1 = "field-value1"
    const val VALUE2 = "field-value2"
    const val TITLE1 = "entry-title1"
    const val TITLE2 = "entry-title2"
    const val CUSTOM_PROPERTY_NAME = "custom-property-name"
    const val CUSTOM_PROPERTY_VALUE = "custom-property-value"
    val UUID1 = UUID.fromString("00000000-0000-0000-0000-000000000001")
    val UUID_CHILD = UUID.fromString("00000000-0000-0000-0000-100000000001")
    val UUID_PARENT = UUID.fromString("00000000-0000-0000-0000-100000000002")

    const val INDENT_EMPTY = ""
    val INDENT_SINGLE = ".".repeat(4)
    val INDENT_DOUBLE = ".".repeat(8)

    val OLD_KEY = newBinary("old-key.ssh", "Old key content")
    val NEW_KEY = newBinary("new-key.ssh", "New key content")

    val BINARY_1 = newBinary(index = 1)
    val BINARY_2 = newBinary(index = 2)
    val BINARY_3 = newBinary(index = 3)
    val BINARY_4 = newBinary(index = 4)
    val BINARY_5 = newBinary(index = 5)
    val BINARY_6 = newBinary(index = 6)

    val GROUP_ROOT = newGroup(name = "Database")
    val GROUP_EMAIL = newGroup(name = "Email")
    val GROUP_INTERNET = newGroup(name = "Internet")
    val GROUP_CODING = newGroup(name = "Coding")
    val GROUP_SHOPPING = newGroup(name = "Shopping")
    val GROUP_SOCIAL = newGroup(name = "Social")
    val GROUP_KEYS = newGroup(name = "Keys")

    val ENTRY_NAS_LOGIN = newEntry(
        title = "NAS Login",
        username = "john.doe",
        password = "abc123"
    )

    val ENTRY_LAPTOP_LOGIN = newEntry(
        title = "Laptop login",
        username = "john.doe",
        password = "abc123"
    )

    val ENTRY_GOOGLE = newEntry(
        title = "Google",
        username = "john.doe@example.com",
        password = "abc123",
        url = "google.com"
    )

    val ENTRY_GOOGLE_MODIFIED = ENTRY_GOOGLE.copy(
        fields = ENTRY_GOOGLE.fields.plus(
            Fields.FIELD_NOTES to "https://google.com"
        )
    )

    val ENTRY_APPLE = newEntry(
        title = "Apple",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://apple.com",
        notes = "My personal Apple account"
    )

    val ENTRY_MICROSOFT = newEntry(
        title = "Microsoft",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://microsoft.com"
    )

    val ENTRY_LEETCODE = newEntry(
        title = "Leetcode.com",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://leetcode.com"
    )

    val ENTRY_NEETCODE = newEntry(
        title = "Neetcode.com",
        username = "john.doe@example.com",
        url = "https://neetcode.io/practice"
    )

    val ENTRY_GITHUB = newEntry(
        title = "Github.com",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://github.com"
    )

    val ENTRY_GITLAB = newEntry(
        title = "Gitlab",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://gitlab.com"
    )

    val ENTRY_FACEBOOK = newEntry(
        title = "Facebook",
        username = "john.doe@example.com",
        password = "abc123",
        url = "https://facebook.com"
    )

    val ENTRY_CLOUD_KEYS = newEntry(
        title = "Cloud keys",
        binaries = listOf(
            OLD_KEY
        )
    )

    val ENTRY_CLOUD_KEYS_MODIFIED = newEntry(
        title = "Cloud keys",
        binaries = listOf(
            NEW_KEY
        )
    )

    val ENTRY_BINARIES_INSERT = newEntry(
        title = "Binaries insert",
        binaries = listOf()
    )

    val ENTRY_BINARIES_INSERT_MODIFIED = newEntry(
        title = "Binaries insert",
        binaries = listOf(
            BINARY_1,
            BINARY_2
        )
    )

    val ENTRY_BINARIES_DELETE = newEntry(
        title = "Binaries delete",
        binaries = listOf(
            BINARY_3,
            BINARY_4
        )
    )

    val ENTRY_BINARIES_DELETE_MODIFIED = newEntry(
        title = "Binaries delete",
        binaries = listOf()
    )

    val ENTRY_BINARIES_CHANGE = newEntry(
        title = "Binaries change",
        binaries = listOf(
            BINARY_5
        )
    )

    val ENTRY_BINARIES_CHANGE_MODIFIED = newEntry(
        title = "Binaries change",
        binaries = listOf(
            BINARY_6
        )
    )

    val ENTRY_UUID_CHANGE = newEntry(
        title = "UUID change"
    )

    val ENTRY_UUID_CHANGE_MODIFIED = ENTRY_UUID_CHANGE.copy(
        uuid = UUID(100L, 1L)
    )

    val GROUP_UUID_CHANGE = newGroup(
        name = "UUID change group"
    )

    val GROUP_UUID_CHANGE_MODIFIED = GROUP_UUID_CHANGE.copy(
        uuid = UUID(200L, 1L)
    )
}