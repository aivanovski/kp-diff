package com.github.ai.kpdiff.entity

import kotlin.Exception

// TODO: write tests
sealed class Either<out T : Any?> {

    data class Left(val exception: Exception) : Either<Nothing>()
    data class Right<T>(val data: T) : Either<T>()

    fun isLeft() = (this is Left)
    fun isRight() = (this is Right)
    fun unwrap(): T = (this as Right).data
    fun unwrapError(): Exception = (this as Left).exception
    fun mapToLeft(): Left = (this as Left)
}