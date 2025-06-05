package com.github.ai.kpdiff.utils

import arrow.core.Either as ArrowEither
import com.github.ai.kpdiff.entity.Either
import com.github.ai.kpdiff.entity.Either.Left
import com.github.ai.kpdiff.entity.Either.Right

fun <E, V : Any?> ArrowEither<E, V>.unwrap(): V {
    return getOrNull() as V
}

fun <E, V> ArrowEither<E, V>.unwrapError(): E {
    val error = leftOrNull()
    requireNotNull(error)
    return error
}

fun <E : Exception, V> ArrowEither<E, V>.toDomainEither(): Either<V> {
    return when (this) {
        is ArrowEither.Left -> Left(exception = unwrapError())
        is ArrowEither.Right -> Right(data = unwrap())
    }
}