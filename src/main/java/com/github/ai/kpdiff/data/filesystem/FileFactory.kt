package com.github.ai.kpdiff.data.filesystem

import java.io.File

interface FileFactory {
    fun newFile(path: String): File
}