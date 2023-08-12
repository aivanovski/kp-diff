package com.github.ai.kpdiff.di

import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.data.filesystem.FileSystemProviderImpl
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.data.keepass.KotpassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.MainInteractor
import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDifferProvider
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.diff.formatter.DiffFormatterImpl
import com.github.ai.kpdiff.domain.diff.formatter.EntityFormatterProvider
import com.github.ai.kpdiff.domain.diff.formatter.ParentFormatter
import com.github.ai.kpdiff.domain.diff.formatter.TerminalOutputFormatter
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.output.StdoutOutputPrinter
import com.github.ai.kpdiff.domain.usecases.DetermineInputTypeUseCase
import com.github.ai.kpdiff.domain.usecases.GetKeysUseCase
import com.github.ai.kpdiff.domain.usecases.GetVersionUseCase
import com.github.ai.kpdiff.domain.usecases.OpenDatabasesUseCase
import com.github.ai.kpdiff.domain.usecases.PrintDiffUseCase
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.PrintVersionUseCase
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import org.koin.dsl.module

object KoinModule {

    val appModule = module {
        single { InputReaderFactory() }
        single<OutputPrinter> { StdoutOutputPrinter() }
        single { ErrorHandler(get()) }
        single<FileSystemProvider> { FileSystemProviderImpl() }
        single<KeepassDatabaseFactory> { KotpassDatabaseFactory(get()) }
        single { DatabaseDifferProvider() }
        single { EntityFormatterProvider() }
        single { TerminalOutputFormatter() }
        single { ParentFormatter() }
        single<DiffFormatter> { DiffFormatterImpl(get(), get(), get()) }
        single { ArgumentParser(get()) }

        // use cases
        single { DetermineInputTypeUseCase() }
        single { ReadPasswordUseCase(get(), get(), get(), get(), get()) }
        single { GetVersionUseCase() }
        single { PrintHelpUseCase(get()) }
        single { PrintVersionUseCase(get()) }
        single { GetKeysUseCase(get()) }
        single { OpenDatabasesUseCase(get()) }
        single { PrintDiffUseCase(get(), get()) }

        single { MainInteractor(get(), get(), get(), get(), get(), get(), get(), get()) }
    }
}