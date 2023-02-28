package com.github.ai.kpdiff.di

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier

object GlobalInjector {

    inline fun <reified T : Any> inject(
        qualifier: Qualifier? = null
    ): Lazy<T> = GlobalContext.get().inject(qualifier)

    inline fun <reified T : Any> get(
        qualifier: Qualifier? = null,
        params: ParametersHolder? = null
    ): T = GlobalContext.get().get(
        qualifier,
        parameters = if (params != null) {
            { params }
        } else {
            null
        }
    )
}