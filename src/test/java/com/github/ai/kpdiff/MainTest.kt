package com.github.ai.kpdiff

import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.createModifiedDatabase
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class MainTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `application should work with password`() {
        val lhsFile = tempDir.resolve("lhs.kdbx").toFile()
        val rhsFile = tempDir.resolve("rhs.kdbx").toFile()
        val outputFile = tempDir.resolve("output.patch").toFile()

        val lhsBytes = createDatabase().toByteArray()
        lhsFile.writeBytes(lhsBytes)

        val rhsBytes = createModifiedDatabase(PASSWORD_KEY).toByteArray()
        rhsFile.writeBytes(rhsBytes)

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

        outputFile.readLines().forEach {
            println(it)
        }
    }
}