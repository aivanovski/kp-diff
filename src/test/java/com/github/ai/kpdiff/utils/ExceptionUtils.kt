package com.github.ai.kpdiff.utils

import java.io.ByteArrayOutputStream
import java.io.PrintStream

fun formatStackTrace(exception: Exception): String {
    val stream = ByteArrayOutputStream(4096)
    exception.printStackTrace(PrintStream(stream))
    return stream.toString()
}