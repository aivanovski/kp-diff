package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.domain.argument.ArgumentParser
import com.github.ai.kpdiff.domain.diff.DatabaseDifferProvider
import com.github.ai.kpdiff.domain.output.OutputPrinter
import com.github.ai.kpdiff.domain.usecases.GetKeysUseCase
import com.github.ai.kpdiff.domain.usecases.OpenDatabasesUseCase
import com.github.ai.kpdiff.domain.usecases.PrintDiffUseCase
import com.github.ai.kpdiff.domain.usecases.PrintHelpUseCase
import com.github.ai.kpdiff.domain.usecases.PrintVersionUseCase
import com.github.ai.kpdiff.entity.DiffFormatterOptions
import com.github.ai.kpdiff.entity.DifferType
import com.github.ai.kpdiff.entity.Either

class MainInteractor(
    private val argumentParser: ArgumentParser,
    private val printHelpUseCase: PrintHelpUseCase,
    private val printVersionUseCase: PrintVersionUseCase,
    private val getKeysUseCase: GetKeysUseCase,
    private val openDatabasesUseCase: OpenDatabasesUseCase,
    private val printDiffUseCase: PrintDiffUseCase,
    private val differProvider: DatabaseDifferProvider,
    private val printer: OutputPrinter
) {

    fun process(rawArgs: Array<String>): Either<Unit> {
        val args = argumentParser.parse(rawArgs)
        if (args.isLeft()) {
            return args.mapToLeft()
        }

        val parsedArgs = args.unwrap()
        if (parsedArgs.isPrintHelp) {
            printHelpUseCase.printHelp(printer)
            return Either.Right(Unit)
        }

        if (parsedArgs.isPrintVersion) {
            printVersionUseCase.printVersion(printer)
            return Either.Right(Unit)
        }

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
        val diff = differProvider
            .getDiffer(parsedArgs.differType ?: DifferType.PATH)
            .getDiff(lhs, rhs)

        printDiffUseCase.printDiff(
            diff = diff,
            options = DiffFormatterOptions(
                isColorEnabled = !parsedArgs.isNoColoredOutput,
                isVerboseOutput = parsedArgs.isVerboseOutput
            )
        )

        return Either.Right(Unit)
    }
}