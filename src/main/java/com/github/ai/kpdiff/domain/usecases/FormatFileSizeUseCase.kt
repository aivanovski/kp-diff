package com.github.ai.kpdiff.domain.usecases

import java.lang.Long.numberOfLeadingZeros
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class FormatFileSizeUseCase {

    @Suppress("MagicNumber")
    fun formatHumanReadableFileSize(byteCount: Long): String {
        require(byteCount >= 0) { "Invalid file size: $byteCount" }

        if (byteCount < ONE_KILO_BYTE) {
            return "$byteCount Bytes"
        }

        val unitIdx = (63 - numberOfLeadingZeros(byteCount)) / 10
        val divider = 1L shl (unitIdx * 10)
        val unitName = " KMGTPE"[unitIdx].toString() + "B"

        return DECIMAL_FORMAT.format(byteCount.toDouble() / divider) + " " + unitName
    }

    companion object {

        private const val ONE_KILO_BYTE = 1024
        private val DECIMAL_FORMAT = createDecimalFormat()

        private fun createDecimalFormat(): DecimalFormat {
            val otherSymbols = DecimalFormatSymbols(Locale.US)
                .apply {
                    decimalSeparator = '.'
                    groupingSeparator = ','
                }

            return DecimalFormat("#.##", otherSymbols)
        }
    }
}