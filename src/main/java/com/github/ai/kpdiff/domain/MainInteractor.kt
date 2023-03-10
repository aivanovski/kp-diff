package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey

class MainInteractor(
    private val argumentParser: ArgumentParser,
    private val readPasswordUseCase: ReadPasswordUseCase,
    private val printHelpUseCase: PrintHelpUseCase,
    private val dbFactory: KeepassDatabaseFactory,
    private val differ: DatabaseDiffer,
    private val diffFormatter: DiffFormatter,
    private val printer: OutputPrinter
) {

    // TODO: write tests
    fun process(rawArgs: Array<String>): Either<Unit> {
        if (printHelpUseCase.shouldPrintHelp(rawArgs)) {
            printHelpUseCase.printHelp(printer)
            return Either.Right(Unit)
        }

        val args = argumentParser.parse(rawArgs)
        if (args.isLeft()) {
            return args.mapToLeft()
        }

        val parsedArgs = args.unwrap()
        val (lhsPassword, rhsPassword) = if (parsedArgs.isUseOnePassword) {
            val password = readPasswordUseCase.readPassword(
                listOf(parsedArgs.leftPath, parsedArgs.rightPath)
            )
            if (password.isLeft()) {
                return password.mapToLeft()
            }

            Pair(password.unwrap(), password.unwrap())
        } else {
            val lhsPassword = readPasswordUseCase.readPassword(listOf(parsedArgs.leftPath))
            if (lhsPassword.isLeft()) {
                return lhsPassword.mapToLeft()
            }

            val rhsPassword = readPasswordUseCase.readPassword(listOf(parsedArgs.rightPath))
            if (rhsPassword.isLeft()) {
                return rhsPassword.mapToLeft()
            }

            Pair(lhsPassword.unwrap(), rhsPassword.unwrap())
        }

        val lhsKey = KeepassKey.PasswordKey(lhsPassword)
        val rhsKey = KeepassKey.PasswordKey(rhsPassword)

        val lhsDb = dbFactory.createDatabase(args.unwrap().leftPath, lhsKey)
        if (lhsDb.isLeft()) {
            return lhsDb.mapToLeft()
        }

        val rhsDb = dbFactory.createDatabase(args.unwrap().rightPath, rhsKey)
        if (rhsDb.isLeft()) {
            return rhsDb.mapToLeft()
        }

        val lhs = lhsDb.unwrap()
        val rhs = rhsDb.unwrap()
        val diff = differ.getDiff(lhsDb.unwrap(), rhsDb.unwrap())
        val formatterOptions = DiffFormatterOptions(isColorEnabled = true)
        val diffLines = diffFormatter.format(diff, lhs, rhs, formatterOptions)
        diffLines.forEach { printer.printLine(it) }

        return Either.Right(Unit)
    }
}