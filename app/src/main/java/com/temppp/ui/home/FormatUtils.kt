package com.temppp.ui.home

fun formatCoins(value: Long): String {
    val (divisor, suffix) = when {
        value >= 1_000_000_000_000L -> 1_000_000_000_000L to "t"
        value >= 1_000_000_000L     -> 1_000_000_000L     to "b"
        value >= 1_000_000L         -> 1_000_000L         to "m"
        value >= 1_000L             -> 1_000L             to "k"
        else                        -> return value.toString()
    }
    val whole = value / divisor
    val remainder = value % divisor
    val decimal = remainder * 1000 / divisor  // 3 decimal digits
    return if (decimal == 0L) {
        "$whole$suffix"
    } else {
        val decStr = decimal.toString().trimEnd('0')
        "$whole,${decStr}$suffix"
    }
}
