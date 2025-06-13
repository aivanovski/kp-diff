package com.github.ai.kpdiff

import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.createModifiedDatabase
import com.github.ai.kpdiff.TestData.GROUP_EMAIL
import com.github.ai.kpdiff.TestData.GROUP_ROOT
import com.github.ai.kpdiff.TestEntityFactory.newEntry
import com.github.ai.kpdiff.testUtils.toBuilderEntity
import com.github.aivanovski.keepasstreebuilder.DatabaseBuilderDsl
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class MainTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `application should work with password`() {
        // arrange
        val (lhsFile, rhsFile, outputFile) = setupFiles()

        createDatabase()
            .toByteArray()
            .writeInto(lhsFile)

        createModifiedDatabase(PASSWORD_KEY)
            .toByteArray()
            .writeInto(rhsFile)

        // act
        main(
            arrayOf(
                lhsFile.path,
                rhsFile.path,
                "--password", TestData.PASSWORD,
                "--output-file", outputFile.path,
                "--no-color",
                "--verbose",
            )
        )

        // assert
        outputFile.readLines().forEach {
            println(it)
        }
    }

    @Test
    fun `application should `() {
        // arrange
        val (lhsFile, rhsFile, outputFile) = setupFiles()

        DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(PASSWORD_KEY)
            .content(GROUP_ROOT.toBuilderEntity()) {
                entry(
                    newEntry(
                        title = "Entry 1",
                        custom = mapOf(
                            "custom-key-1" to "custom-value-1",
                        )
                    ).toBuilderEntity()
                )
            }
            .build()
            .toByteArray()
            .writeInto(lhsFile)

        DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(PASSWORD_KEY)
            .content(GROUP_ROOT.toBuilderEntity()) {
                group(GROUP_EMAIL.toBuilderEntity()) {
                    entry(
                        newEntry(
                            title = "Entry 1",
                            custom = mapOf(
                                "custom-key-2" to "custom-value-2",
                            )
                        ).toBuilderEntity()
                    )
                }
            }
            .build()
            .toByteArray()
            .writeInto(rhsFile)

        // act
        main(
            arrayOf(
                lhsFile.path,
                rhsFile.path,
                "--password", TestData.PASSWORD,
                "--output-file", outputFile.path,
                "--no-color",
                "--verbose",
            )
        )

        // assert
        outputFile.readLines().forEach {
            println(it)
        }
    }

    private fun ByteArray.writeInto(file: File) {
        file.writeBytes(this)
    }

    private fun setupFiles(): Triple<File, File, File> {
        val lhsFile = tempDir.resolve("lhs.kdbx").toFile()
        val rhsFile = tempDir.resolve("rhs.kdbx").toFile()
        val outputFile = tempDir.resolve("output.patch").toFile()

        return Triple(lhsFile, rhsFile, outputFile)
    }
}