package com.github.ai.kpdiff.testUtils

import java.io.ByteArrayInputStream
import java.io.InputStream

fun String.toInputStream(): InputStream {
    return ByteArrayInputStream(this.toByteArray())
}