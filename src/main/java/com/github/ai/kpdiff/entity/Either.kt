package com.github.ai.kpdiff.entity

import kotlin.Exception

sealed class Either<out T : Any?> {

    data class Left(val exception: Exception) : Either<Nothing>()
    data class Right<T>(val data: T) : Either<T>()

    fun isLeft() = (this is Left)
    fun isRight() = (this is Right)
    fun unwrap(): T = (this as Right).data
    fun unwrapError(): Exception = (this as Left).exception
    fun mapToLeft(): Left = (this as Left)
    fun <T2> mapWith(newData: T2): Either<T2> {
        return if (isRight()) {
            Right(newData)
        } else {
            mapToLeft()
        }
    }
}