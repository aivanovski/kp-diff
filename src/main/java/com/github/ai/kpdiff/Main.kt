package com.github.ai.kpdiff

import com.github.ai.kpdiff.di.KoinModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(KoinModule.appModule)
    }
}