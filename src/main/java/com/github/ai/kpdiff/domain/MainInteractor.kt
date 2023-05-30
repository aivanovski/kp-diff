package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.usecases.*
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.Either

class MainInteractor(
    private val argumentParser: ArgumentParser,
    private val printHelpUseCase: PrintHelpUseCase,
    private val printVersionUseCase: PrintVersionUseCase,
    private val getKeysUseCase: GetKeysUseCase,
    private val openDatabasesUseCase: OpenDatabasesUseCase,
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

        if (printVersionUseCase.shouldPrintVersion(rawArgs)) {
            printVersionUseCase.printVersion(printer)
            return Either.Right(Unit)
        }

        val args = argumentParser.parse(rawArgs)
        if (args.isLeft()) {
            return args.mapToLeft()
        }

        val parsedArgs = args.unwrap()

        val keys = getKeysUseCase.getKeys(parsedArgs)
        if (keys.isLeft()) {
            return keys.mapToLeft()
        }

        val (lhsKey, rhsKey) = keys.unwrap()

        val databases = openDatabasesUseCase.openDatabases(
            leftPath = parsedArgs.leftPath,
            leftKey = lhsKey,
            rightPath = parsedArgs.rightPath,
            rightKey = rhsKey
        )
        if (databases.isLeft()) {
            return databases.mapToLeft()
        }

        val (lhs, rhs) = databases.unwrap()
        val diff = differ.getDiff(lhs, rhs)
        val formatterOptions = DiffFormatterOptions(
            isColorEnabled = !parsedArgs.isNoColoredOutput
        )
        val diffLines = diffFormatter.format(diff, lhs, rhs, formatterOptions)
        diffLines.forEach { printer.printLine(it) }

        return Either.Right(Unit)
    }
}