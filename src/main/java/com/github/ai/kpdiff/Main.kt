package com.github.ai.kpdiff

import com.github.ai.kpdiff.di.GlobalInjector.get
import com.github.ai.kpdiff.di.KoinModule
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.MainInteractor
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(KoinModule.appModule)
    }

    val interactor: MainInteractor = get()
    val errorHandler: ErrorHandler = get()
    val result = interactor.process(args)

    errorHandler.handleIfLeft(result)
}