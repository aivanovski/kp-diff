package com.github.ai.kpdiff.testUtils

import java.io.ByteArrayInputStream
import java.io.InputStream

fun Any.resourceAsBytes(name: String): ByteArray {
    val stream = this.javaClass.classLoader.getResourceAsStream(name)
    checkNotNull(stream)

    return stream.readAllBytes()
}

fun Any.resourceAsString(name: String): String =
    String(resourceAsBytes(name))

fun Any.resourceAsStream(name: String): InputStream =
    ByteArrayInputStream(resourceAsBytes(name))

fun InputStream.readText(): String {
    return String(readAllBytes())
}