package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.Arguments
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.KeepassKey
import com.github.ai.kpdiff.entity.KeepassKey.FileKey
import com.github.ai.kpdiff.entity.KeepassKey.PasswordKey

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

        val keys = getKeys(parsedArgs)
        if (keys.isLeft()) {
            return keys.mapToLeft()
        }

        val (lhsKey, rhsKey) = keys.unwrap()

        val lhsDb = dbFactory.createDatabase(parsedArgs.leftPath, lhsKey)
        if (lhsDb.isLeft()) {
            return lhsDb.mapToLeft()
        }

        val rhsDb = dbFactory.createDatabase(parsedArgs.rightPath, rhsKey)
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

    private fun getKeys(args: Arguments): Either<Pair<KeepassKey, KeepassKey>> {
        if (args.isUseOnePassword) {
            val password = readPasswordUseCase.readPassword(
                listOf(args.leftPath, args.rightPath)
            )
            if (password.isLeft()) {
                return password.mapToLeft()
            }

            return Either.Right(
                Pair(
                    PasswordKey(password.unwrap()),
                    PasswordKey(password.unwrap())
                )
            )
        }

        val keys = mutableListOf<KeepassKey>()
        val pathToKeyPathPairs = listOf(
            Pair(args.leftPath, args.leftKeyPath),
            Pair(args.rightPath, args.rightKeyPath)
        )

        for ((path, keyPath) in pathToKeyPathPairs) {
            if (keyPath != null) {
                keys.add(FileKey(keyPath))
            } else {
                val password = readPasswordUseCase.readPassword(listOf(path))
                if (password.isLeft()) {
                    return password.mapToLeft()
                }

                keys.add(PasswordKey(password.unwrap()))
            }
        }

        return Either.Right(
            Pair(keys[0], keys[1])
        )
    }
}