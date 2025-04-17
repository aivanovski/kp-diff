package com.github.ai.kpdiff.domain.usecases

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.ai.kpdiff.DatabaseFactory.COMPOSITE_KEY
import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.toDomainDatabase
import com.github.ai.kpdiff.TestData.FILE_NAME
import com.github.ai.kpdiff.TestData.FILE_PATH
import com.github.ai.kpdiff.TestData.INVALID_PASSWORD
import com.github.ai.kpdiff.TestData.KEY_PATH
import com.github.ai.kpdiff.TestData.PASSWORD
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.data.keepass.KotpassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD_FOR_FILE
import com.github.ai.kpdiff.domain.input.InputReader
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType.STANDARD
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.exception.TooManyAttemptsException
import com.github.ai.kpdiff.testUtils.MockedFileSystemProvider
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.model.Database
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import java.io.FileNotFoundException
import java.lang.Exception
import org.junit.jupiter.api.Test

internal class ReadPasswordUseCaseTest {

    private val determineInputTypeUseCase: DetermineInputTypeUseCase = mockk()
    private val dbFactory: KeepassDatabaseFactory = mockk()
    private val inputReaderFactory: InputReaderFactory = mockk()
    private val errorHandler: ErrorHandler = mockk()
    private val outputPrinter: OutputPrinter = mockk()
    private val inputReader: InputReader = mockk()

    @Test
    fun `readPassword should return password`() {
        // arrange
        val db = createDatabase(PASSWORD_KEY)
        val fsProvider = newFsProviderWithDatabase(db)
        val dbFactory = KotpassDatabaseFactory(fsProvider)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(PASSWORD)

        // act
        val result = newUseCase(
            fsProvider = fsProvider,
            dbFactory = dbFactory
        ).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = false
        )

        // assert
        result shouldBe Either.Right(PASSWORD)
    }

    @Test
    fun `readPassword should return password for composite key`() {
        // arrange
        val db = createDatabase(COMPOSITE_KEY)
        val fsProvider = MockedFileSystemProvider(
            content = mapOf(
                FILE_PATH to db.toByteArray(),
                KEY_PATH to COMPOSITE_KEY.binaryData
            )
        )
        val dbFactory = KotpassDatabaseFactory(fsProvider)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(PASSWORD)

        // act
        val result = newUseCase(
            fsProvider = fsProvider,
            dbFactory = dbFactory
        ).readPassword(
            path = FILE_PATH,
            keyPath = KEY_PATH,
            isPrintFileName = false
        )

        // assert
        result shouldBe Either.Right(PASSWORD)
    }

    @Test
    fun `readPassword should ask password second time if it is invalid`() {
        // arrange
        val error = Either.Left(Exception())
        val db = createDatabase(PASSWORD_KEY)
        val fsProvider = newFsProviderWithDatabase(db)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(INVALID_PASSWORD).andThen(PASSWORD)
        every { dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD)) }.returns(error)
        every {
            dbFactory.createDatabase(
                FILE_PATH,
                newKey(PASSWORD)
            )
        }.returns(Either.Right(db.toDomainDatabase()))
        every { errorHandler.handleIfLeft(error) }.returns(Unit)

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = false
        )

        // assert
        verifySequence {
            determineInputTypeUseCase.getInputReaderType()
            inputReaderFactory.createReader(STANDARD)
            outputPrinter.printLine(ENTER_A_PASSWORD)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(ENTER_A_PASSWORD)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD))
        }
        result shouldBe Either.Right(PASSWORD)
    }

    @Test
    fun `readPassword should return error if all attempts failed`() {
        // arrange
        val error = Either.Left(Exception())
        val db = createDatabase(PASSWORD_KEY)
        val fsProvider = newFsProviderWithDatabase(db)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(INVALID_PASSWORD)
        every { dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD)) }.returns(error)
        every { errorHandler.handleIfLeft(error) }.returns(Unit)

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = false
        )

        // assert
        verifySequence {
            determineInputTypeUseCase.getInputReaderType()
            inputReaderFactory.createReader(STANDARD)
            outputPrinter.printLine(ENTER_A_PASSWORD)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(ENTER_A_PASSWORD)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(ENTER_A_PASSWORD)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
        }
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<TooManyAttemptsException>()
    }

    @Test
    fun `readPassword should return error if file is missing`() {
        // arrange
        val fsProvider = MockedFileSystemProvider()

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = true
        )

        // assert
        result.isLeft() shouldBe true
        result.unwrapError() should beInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `readPassword should print valid message`() {
        listOf(
            true to String.format(ENTER_A_PASSWORD_FOR_FILE, FILE_NAME),
            false to ENTER_A_PASSWORD
        ).forEach { (isShowFileName, message) ->
            // arrange
            val outputPrinter = mockk<OutputPrinter>()
            val db = createDatabase(PASSWORD_KEY)
            val fsProvider = newFsProviderWithDatabase(db)
            every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
            every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
            every { outputPrinter.printLine(message) }.returns(Unit)
            every { inputReader.read() }.returns(PASSWORD)
            every {
                dbFactory.createDatabase(
                    FILE_PATH,
                    newKey(PASSWORD)
                )
            }.returns(Either.Right(db.toDomainDatabase()))

            // act
            val result = newUseCase(
                fsProvider = fsProvider,
                outputPrinter = outputPrinter
            ).readPassword(
                path = FILE_PATH,
                keyPath = null,
                isPrintFileName = isShowFileName
            )

            // assert
            verify {
                outputPrinter.printLine(message)
            }
            result shouldBe Either.Right(PASSWORD)
        }
    }

    private fun newKey(password: String): KeepassKey.PasswordKey =
        KeepassKey.PasswordKey(password)

    private fun newFsProviderWithDatabase(
        db: Database<DatabaseElement, KeePassDatabase>
    ): FileSystemProvider =
        MockedFileSystemProvider(
            content = mapOf(
                FILE_PATH to db.toByteArray()
            )
        )

    private fun newUseCase(
        fsProvider: FileSystemProvider,
        determineInputTypeUseCase: DetermineInputTypeUseCase = this.determineInputTypeUseCase,
        dbFactory: KeepassDatabaseFactory = this.dbFactory,
        inputReaderFactory: InputReaderFactory = this.inputReaderFactory,
        errorHandler: ErrorHandler = this.errorHandler,
        outputPrinter: OutputPrinter = this.outputPrinter
    ): ReadPasswordUseCase =
        ReadPasswordUseCase(
            fileSystemProvider = fsProvider,
            determineInputTypeUseCase = determineInputTypeUseCase,
            dbFactory = dbFactory,
            inputReaderFactory = inputReaderFactory,
            errorHandler = errorHandler,
            printer = outputPrinter
        )
}