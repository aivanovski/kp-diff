package com.github.ai.kpdiff.entity.exception

import com.github.ai.kpdiff.domain.Strings
import java.lang.Exception

open class KpDiffException(message: String) : Exception(message)

class TooManyAttemptsException : KpDiffException(Strings.TOO_MANY_ATTEMPTS)