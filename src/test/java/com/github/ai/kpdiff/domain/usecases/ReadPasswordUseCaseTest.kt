package com.github.ai.kpdiff.domain.usecases

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.ai.kpdiff.DatabaseFactory
import com.github.ai.kpdiff.DatabaseFactory.COMPOSITE_KEY
import com.github.ai.kpdiff.DatabaseFactory.PASSWORD_KEY
import com.github.ai.kpdiff.DatabaseFactory.createDatabase
import com.github.ai.kpdiff.DatabaseFactory.toDomainDatabase
import com.github.ai.kpdiff.TestData
import com.github.ai.kpdiff.TestData.FILE_NAME
import com.github.ai.kpdiff.TestData.FILE_PATH
import com.github.ai.kpdiff.TestData.INVALID_PASSWORD
import com.github.ai.kpdiff.TestData.PASSWORD
import com.github.ai.kpdiff.data.filesystem.FileSystemProvider
import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.ErrorHandler
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD
import com.github.ai.kpdiff.domain.Strings.ENTER_A_PASSWORD_FOR_FILE
import com.github.ai.kpdiff.domain.input.InputReader
import com.github.ai.kpdiff.domain.input.InputReaderFactory
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType.STANDARD
import com.github.ai.kpdiff.entity.KeepassDatabase
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.exception.KpDiffException
import com.github.ai.kpdiff.entity.exception.TooManyAttemptsException
import com.github.ai.kpdiff.testUtils.MockedFileSystemProvider
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
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
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(PASSWORD)
        every {
            dbFactory.createDatabase(
                FILE_PATH,
                newKey(PASSWORD)
            )
        }.returns(Either.Right(db.toDomainDatabase()))

        // act
        val result = newUseCase(fsProvider).readPassword(
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
        val db = Either.Right(mockk<KeepassDatabase>())
        val fsProvider = newFsProviderWithDatabase(COMPOSITE_KEY)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
        every { inputReader.read() }.returns(PASSWORD)
        every { dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD)) }.returns(db)

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = false
        )

        // assert
        result shouldBe Either.Right(PASSWORD)
    }

    @Test
    fun `readPassword should ask password second time if it is invalid`() {
        // arrange
        val message = String.format(ENTER_A_PASSWORD_FOR_FILE, FILE_NAME)
        val db = createDatabase(PASSWORD_KEY)
        val error = Either.Left(Exception())
        val fsProvider = newFsProviderWithDatabase(PASSWORD_KEY)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(message) }.returns(Unit)
        every { inputReader.read() }.returns(INVALID_PASSWORD).andThen(PASSWORD)
        every { dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD)) }.returns(error)
        every { dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD)) }.returns(db)
        every { errorHandler.handleIfLeft(error) }.returns(Unit)

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = true
        )

        // assert
        verifySequence {
            determineInputTypeUseCase.getInputReaderType()
            inputReaderFactory.createReader(STANDARD)
            outputPrinter.printLine(message)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(message)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD))
        }
        result shouldBe Either.Right(PASSWORD)
    }

    @Test
    fun `readPassword should return error if password is invalid`() {
        // arrange
        val message = String.format(ENTER_A_PASSWORD_FOR_FILE, FILE_NAME)
        val error = Either.Left(Exception())
        val fsProvider = newFsProviderWithDatabase(PASSWORD_KEY)
        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
        every { outputPrinter.printLine(message) }.returns(Unit)
        every { inputReader.read() }.returns(INVALID_PASSWORD)
        every { dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD)) }.returns(error)
        every { errorHandler.handleIfLeft(error) }.returns(Unit)

        // act
        val result = newUseCase(fsProvider).readPassword(
            path = FILE_PATH,
            keyPath = null,
            isPrintFileName = true
        )

        // assert
        verifySequence {
            determineInputTypeUseCase.getInputReaderType()
            inputReaderFactory.createReader(STANDARD)
            outputPrinter.printLine(message)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(message)
            inputReader.read()
            dbFactory.createDatabase(FILE_PATH, newKey(INVALID_PASSWORD))
            errorHandler.handleIfLeft(error)
            outputPrinter.printLine(message)
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
            val db = Either.Right(mockk<KeepassDatabase>())
            val fsProvider = newFsProviderWithDatabase(PASSWORD_KEY)
            every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
            every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
            every { outputPrinter.printLine(message) }.returns(Unit)
            every { inputReader.read() }.returns(PASSWORD)
            every { dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD)) }.returns(db)

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


//    @Test
//    fun `readPassword should return password for multiple files`() {
//        // arrange
//        val db = Either.Right(mockk<KeepassDatabase>())
//        val anotherDb = Either.Right(mockk<KeepassDatabase>())
//        every { determineInputTypeUseCase.getInputReaderType() }.returns(STANDARD)
//        every { inputReaderFactory.createReader(STANDARD) }.returns(inputReader)
//        every { outputPrinter.printLine(ENTER_A_PASSWORD) }.returns(Unit)
//        every { inputReader.read() }.returns(PASSWORD)
//        every { dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD)) }.returns(db)
//        every { dbFactory.createDatabase(ANOTHER_FILE_PATH, newKey(PASSWORD)) }.returns(anotherDb)
//
//        // act
//        val result = newUseCase().readPassword(
//            path = FILE_PATH,
//            keyPath = null,
//            isShowFileName = true
//        )
//
//        // assert
//        verifySequence {
//            determineInputTypeUseCase.getInputReaderType()
//            inputReaderFactory.createReader(STANDARD)
//            outputPrinter.printLine(ENTER_A_PASSWORD)
//            inputReader.read()
//            dbFactory.createDatabase(FILE_PATH, newKey(PASSWORD))
//            dbFactory.createDatabase(ANOTHER_FILE_PATH, newKey(PASSWORD))
//        }
//        result shouldBe Either.Right(PASSWORD)
//    }

    private fun newKey(password: String): KeepassKey.PasswordKey =
        KeepassKey.PasswordKey(password)

//    private fun newCompositeKey(): KeepassKey.CompositeKey =
//        KeepassKey.CompositeKey()

    private fun newFsProviderWithDatabase(
        db: Database<DatabaseElement, KeePassDatabase>
    ): FileSystemProvider =
        MockedFileSystemProvider(
            content = mapOf(
                FILE_PATH to db.toByteArray()
            )
        )

//    private fun newDbAndFsProvider(key: DatabaseKey): Pair<KeepassDatabase, FileSystemProvider> {
//        val db = createDatabase(key)
//        return newFsProviderWithDatabase(db)
//    }

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

    companion object {
        private const val ANOTHER_FILE_PATH = "/path/to/another.kdbx"
    }
}