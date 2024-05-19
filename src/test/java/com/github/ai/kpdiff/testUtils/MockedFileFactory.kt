package com.github.ai.kpdiff.testUtils

import com.github.ai.kpdiff.data.filesystem.FileFactory
import java.io.File

class MockedFileFactory : FileFactory {
    override fun newFile(path: String): File {
        return File(path)
    }
}