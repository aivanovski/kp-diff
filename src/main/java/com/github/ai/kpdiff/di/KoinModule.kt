package com.github.ai.kpdiff.di

import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.MainInteractor
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputWriter
import com.github.ai.kpdiff.domain.output.StdoutOutputWriter
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import org.koin.dsl.module

object KoinModule {

    val appModule = module {
        single { InputReaderFactory() }
        single<OutputWriter> { StdoutOutputWriter() }
        single { ErrorHandler(get()) }

        // use cases
        single { ReadPasswordUseCase(get()) }

        single { MainInteractor(get()) }
    }
}