package com.github.ai.kpdiff.domain

import com.github.ai.kpdiff.data.keepass.KeepassDatabaseFactory
import com.github.ai.kpdiff.domain.diff.DatabaseDiffer
import com.github.ai.kpdiff.domain.diff.DiffFormatter
import com.github.ai.kpdiff.domain.output.OutputWriter
import com.github.ai.kpdiff.domain.usecases.ReadPasswordUseCase
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.InputReaderType
import com.github.ai.kpdiff.entity.KeepassKey

class MainInteractor(
    private val readPasswordUseCase: ReadPasswordUseCase,
    private val dbFactory: KeepassDatabaseFactory,
    private val differ: DatabaseDiffer,
    private val diffFormatter: DiffFormatter,
    private val writer: OutputWriter
) {

    // TODO: write tests
    fun process(args: Array<String>): Either<Unit> {
        val password = readPasswordUseCase.readPassword(InputReaderType.STANDARD)
        if (password.isLeft()) {
            return password.mapToLeft()
        }

        val key = KeepassKey.PasswordKey(password.unwrap())
        val lhsDb = dbFactory.createDatabase(args[0], key)
        if (lhsDb.isLeft()) {
            return lhsDb.mapToLeft()
        }

        val rhsDb = dbFactory.createDatabase(args[1], key)
        if (rhsDb.isLeft()) {
            return rhsDb.mapToLeft()
        }

        val lhs = lhsDb.unwrap()
        val rhs = rhsDb.unwrap()
        val diff = differ.getDiff(lhsDb.unwrap(), rhsDb.unwrap())
        val diffLines = diffFormatter.format(diff, lhs, rhs)
        diffLines.forEach { writer.printLine(it) }

        return Either.Right(Unit)
    }
}