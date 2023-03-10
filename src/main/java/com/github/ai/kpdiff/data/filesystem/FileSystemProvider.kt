package com.github.ai.kpdiff.data.filesystem

import com.github.ai.kpdiff.entity.Either
import java.io.InputStream

interface FileSystemProvider {
    fun exists(path: String): Boolean
    fun open(path: String): Either<InputStream>
}