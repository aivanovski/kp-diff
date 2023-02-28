package com.github.ai.kpdiff.entity

import java.lang.Exception

sealed class Either<out T : Any?> {

    data class Left(val exception: Exception) : Either<Nothing>()
    data class Right<T>(val data: T) : Either<T>()

    fun isLeft() = (this is Left)
    fun isRight() = (this is Right)
    fun unwrap(): T = (this as Right).data
}