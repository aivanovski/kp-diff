package com.github.ai.kpdiff.di

import com.github.ai.kpdiff.data.filesystem.FileFactory
import com.github.ai.kpdiff.data.filesystem.FileFactoryImpl
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
import com.github.ai.kpdiff.domain.usecases.FormatFileSizeUseCase
import com.github.ai.kpdiff.domain.usecases.GetKeysUseCase
import com.github.ai.kpdiff.domain.usecases.GetVersionUseCase
import com.github.ai.kpdiff.domain.usecases.OpenDatabasesUseCase
import com.github.ai.kpdiff.domain.usecases.PrintDiffUseCase
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.PrintVersionUseCase
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.domain.usecases.WriteDiffToFileUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object KoinModule {

    val appModule = module {
        singleOf(::InputReaderFactory)
        singleOf(::StdoutOutputPrinter) bind OutputPrinter::class
        singleOf(::ErrorHandler)
        singleOf(::FileFactoryImpl) bind FileFactory::class
        singleOf(::FileSystemProviderImpl) bind FileSystemProvider::class
        singleOf(::KotpassDatabaseFactory) bind KeepassDatabaseFactory::class
        singleOf(::DatabaseDifferProvider)
        singleOf(::EntityFormatterProvider)
        singleOf(::TerminalOutputFormatter)
        singleOf(::ParentFormatter)
        singleOf(::DiffFormatterImpl) bind DiffFormatter::class
        singleOf(::ArgumentParser)

        // use cases
        singleOf(::DetermineInputTypeUseCase)
        singleOf(::ReadPasswordUseCase)
        singleOf(::GetVersionUseCase)
        singleOf(::PrintHelpUseCase)
        singleOf(::PrintVersionUseCase)
        singleOf(::GetKeysUseCase)
        singleOf(::OpenDatabasesUseCase)
        singleOf(::PrintDiffUseCase)
        singleOf(::WriteDiffToFileUseCase)
        singleOf(::FormatFileSizeUseCase)

        singleOf(::MainInteractor)
    }
}