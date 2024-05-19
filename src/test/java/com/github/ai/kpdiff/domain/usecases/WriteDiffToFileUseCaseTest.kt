package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.TestData.FILE_PATH
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.testUtils.MockedFileSystemProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class WriteDiffToFileUseCaseTest {

    private val diffFormatter: DiffFormatter = mockk()

    @Test
    fun `writeDiff should write diff to file`() {
        // arrange
        val fsProvider = MockedFileSystemProvider()
        val diff = mockk<DiffResult<KeepassDatabase, DatabaseEntity>>()
        every { diffFormatter.format(diff, any()) }.returns(DIFF_CONTENT.split("\n"))

        // act
        val result = newUseCase(fsProvider).writeDiff(diff, FILE_PATH)

        // assert
        result shouldBe Either.Right(Unit)
        fsProvider.read(FILE_PATH) shouldBe DIFF_CONTENT
    }

    private fun newUseCase(
        fileSystemProvider: FileSystemProvider
    ): WriteDiffToFileUseCase {
        return WriteDiffToFileUseCase(
            fileSystemProvider = fileSystemProvider,
            diffFormatter = diffFormatter
        )
    }

    companion object {
        private val DIFF_CONTENT = """
            ~ Group 'My Passwords'
            ~     Group 'Internet'
            ~         Group 'Coding'
            -             Entry 'Github.com'
        """.trimIndent()
    }
}