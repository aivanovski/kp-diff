package com.github.ai.kpdiff.di

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.data.filesystem.FileSystemProviderImpl
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.data.keepass.KotpassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.MainInteractor
import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DatabaseDifferImpl
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.diff.DiffFormatterImpl
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.output.StdoutOutputPrinter
import com.github.ai.kpdiff.domain.usecases.DetermineInputTypeUseCase
import com.github.ai.kpdiff.domain.usecases.GetVersionUseCase
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import org.koin.dsl.module

object KoinModule {

    val appModule = module {
        single { InputReaderFactory() }
        single<OutputPrinter> { StdoutOutputPrinter() }
        single { ErrorHandler(get()) }
        single<FileSystemProvider> { FileSystemProviderImpl() }
        single<KeepassDatabaseFactory> { KotpassDatabaseFactory(get()) }
        single<DatabaseDiffer> { DatabaseDifferImpl() }
        single<DiffFormatter> { DiffFormatterImpl() }
        single { ArgumentParser(get()) }

        // use cases
        single { DetermineInputTypeUseCase() }
        single { ReadPasswordUseCase(get(), get(), get(), get(), get()) }
        single { GetVersionUseCase() }
        single { PrintHelpUseCase(get()) }

        single { MainInteractor(get(), get(), get(), get(), get(), get(), get()) }
    }
}