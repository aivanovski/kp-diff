package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.TestData.LEFT_PASSWORD
import com.github.ai.kpdiff.TestData.RIGHT_PASSWORD
import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DatabaseDifferProvider
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.usecases.GetKeysUseCase
import com.github.ai.kpdiff.domain.usecases.OpenDatabasesUseCase
import com.github.ai.kpdiff.domain.usecases.PrintDiffUseCase
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.PrintVersionUseCase
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.DatabaseEntity
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DiffResult
import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MainInteractorTest {

    private val argumentParser = mockk<ArgumentParser>()
    private val printHelpUseCase = mockk<PrintHelpUseCase>()
    private val printVersionUseCase = mockk<PrintVersionUseCase>()
    private val getKeysUseCase = mockk<GetKeysUseCase>()
    private val openDatabasesUseCase = mockk<OpenDatabasesUseCase>()
    private val printDiffUseCase = mockk<PrintDiffUseCase>()
    private val differProvider = mockk<DatabaseDifferProvider>()
    private val differ = mockk<DatabaseDiffer>()
    private val printer = mockk<OutputPrinter>()

    private val leftKey = PasswordKey(LEFT_PASSWORD)
    private val rightKey = PasswordKey(RIGHT_PASSWORD)
    private val leftDb = mockk<KeepassDatabase>()
    private val rightDb = mockk<KeepassDatabase>()
    private val diff = mockk<DiffResult<KeepassDatabase, DatabaseEntity>>()

    @Test
    fun `process should finish successfully`() {
        listOf(
            DifferType.PATH,
            DifferType.UUID
        ).forEach { differType ->
            // arrange
            val args = newArgs(differType = differType)
            val options = DiffFormatterOptions(isColorEnabled = !args.isNoColoredOutput)
            every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
            every { getKeysUseCase.getKeys(args) }.returns(Either.Right(leftKey to rightKey))
            every {
                openDatabasesUseCase.openDatabases(
                    leftPath = args.leftPath,
                    leftKey = leftKey,
                    rightPath = args.rightPath,
                    rightKey = rightKey
                )
            }.returns(Either.Right(leftDb to rightDb))
            every { differProvider.getDiffer(differType) }.returns(differ)
            every { differ.getDiff(leftDb, rightDb) }.returns(diff)
            every { printDiffUseCase.printDiff(diff, options) }.returns(Unit)

            // act
            val result = newInteractor().process(RAW_ARGS)

            // assert
            result shouldBe Either.Right(Unit)
        }
    }

    @Test
    fun `process should error if unable to parse arguments`() {
        // arrange
        every { argumentParser.parse(RAW_ARGS) }.returns(Either.Left(EXCEPTION))

        // act
        val result = newInteractor().process(RAW_ARGS)

        // assert
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `process should print help and return Unit`() {
        // arrange
        val args = newArgs(isPrintHelp = true)
        every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
        every { printHelpUseCase.printHelp(printer) }.returns(Unit)

        // act
        val result = newInteractor().process(RAW_ARGS)

        // assert
        result shouldBe Either.Right(Unit)
    }

    @Test
    fun `process should print version and return Unit`() {
        // arrange
        val args = newArgs(isPrintVersion = true)
        every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
        every { printVersionUseCase.printVersion(printer) }.returns(Unit)

        // act
        val result = newInteractor().process(RAW_ARGS)

        // assert
        result shouldBe Either.Right(Unit)
    }

    @Test
    fun `process should return error if unable to get keys`() {
        // arrange
        val args = newArgs()
        every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
        every { getKeysUseCase.getKeys(args) }.returns(Either.Left(EXCEPTION))

        // act
        val result = newInteractor().process(RAW_ARGS)

        // assert
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `process should return error if unable to open database`() {
        // arrange
        val args = newArgs()
        val leftKey = PasswordKey(LEFT_PASSWORD)
        val rightKey = PasswordKey(RIGHT_PASSWORD)
        every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
        every { getKeysUseCase.getKeys(args) }.returns(Either.Right(leftKey to rightKey))
        every {
            openDatabasesUseCase.openDatabases(
                leftPath = args.leftPath,
                leftKey = leftKey,
                rightPath = args.rightPath,
                rightKey = rightKey
            )
        }.returns(Either.Left(EXCEPTION))

        // act
        val result = newInteractor().process(RAW_ARGS)

        // assert
        result shouldBe Either.Left(EXCEPTION)
    }

    @Test
    fun `process should pass options if --no-color is specified`() {
        listOf(
            newArgs(isNoColoredOutput = false),
            newArgs(isNoColoredOutput = true)
        ).forEach { args ->
            // arrange
            val options = DiffFormatterOptions(isColorEnabled = !args.isNoColoredOutput)
            every { argumentParser.parse(RAW_ARGS) }.returns(Either.Right(args))
            every { getKeysUseCase.getKeys(args) }.returns(Either.Right(leftKey to rightKey))
            every {
                openDatabasesUseCase.openDatabases(
                    leftPath = args.leftPath,
                    leftKey = leftKey,
                    rightPath = args.rightPath,
                    rightKey = rightKey
                )
            }.returns(Either.Right(leftDb to rightDb))
            every { differProvider.getDiffer(DifferType.PATH) }.returns(differ)
            every { differ.getDiff(leftDb, rightDb) }.returns(diff)
            every { printDiffUseCase.printDiff(diff, options) }.returns(Unit)

            // act
            val result = newInteractor().process(RAW_ARGS)

            // assert
            result shouldBe Either.Right(Unit)
        }
    }

    private fun newArgs(
        isNoColoredOutput: Boolean = false,
        isPrintHelp: Boolean = false,
        isPrintVersion: Boolean = false,
        differType: DifferType? = null
    ): Arguments =
        Arguments(
            leftPath = TestData.LEFT_FILE_PATH,
            rightPath = TestData.RIGHT_FILE_PATH,
            keyPath = null,
            leftKeyPath = null,
            rightKeyPath = null,
            differType = differType,
            isUseOnePassword = false,
            isNoColoredOutput = isNoColoredOutput,
            isPrintHelp = isPrintHelp,
            isPrintVersion = isPrintVersion,
            isVerboseOutput = false
        )

    private fun newInteractor(): MainInteractor =
        MainInteractor(
            argumentParser = argumentParser,
            printHelpUseCase = printHelpUseCase,
            printVersionUseCase = printVersionUseCase,
            getKeysUseCase = getKeysUseCase,
            openDatabasesUseCase = openDatabasesUseCase,
            printDiffUseCase = printDiffUseCase,
            differProvider = differProvider,
            printer = printer
        )

    companion object {
        private val RAW_ARGS = arrayOf("arg1", "arg2")
        private val EXCEPTION = Exception("Test exception")
    }
}