package com.github.ai.kpdiff.domain.usecases

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.utils.StringUtils.NEW_LINE
import java.io.ByteArrayInputStream

class WriteDiffToFileUseCase(
    private val fileSystemProvider: FileSystemProvider,
    private val diffFormatter: DiffFormatter
) {

    fun writeDiff(
        diff: DiffResult<KeepassDatabase, DatabaseEntity>,
        outputPath: String
    ): Either<Unit> {
        val options = DiffFormatterOptions(
            isColorEnabled = false,
            isVerboseOutput = true
        )

        val content = diffFormatter.format(diff, options)
            .joinToString(separator = NEW_LINE)

        return fileSystemProvider.write(
            path = outputPath,
            content = ByteArrayInputStream(content.toByteArray())
        )
    }
}