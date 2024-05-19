package com.github.ai.kpdiff.testUtils

import java.io.IOException
import java.io.InputStream

class ThrowOnReadInputStream(
    private val exception: IOException
) : InputStream() {

    override fun read(): Int {
        throw exception
    }
}