package com.github.ai.kpdiff.domain.usecases

import java.util.Properties

class GetVersionUseCase {

    fun getVersionName(): String {
        val content = GetVersionUseCase::class.java.classLoader
            .getResourceAsStream(VERSION_PROPERTY_FILE_NAME)

        val properties = Properties()
            .apply {
                load(content)
            }

        return properties[PROPERTY_VERSION] as String
    }

    companion object {
        internal const val VERSION_PROPERTY_FILE_NAME = "version.properties"
        internal const val PROPERTY_VERSION = "version"
    }
}