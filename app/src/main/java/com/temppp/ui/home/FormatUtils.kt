package com.temppp.ui.home

fun formatCoins(value: Long): String = when {
    value >= 1_000_000_000_000L -> "${value / 1_000_000_000_000L}T"
    value >= 1_000_000_000L     -> "${value / 1_000_000_000L}B"
    value >= 1_000_000L         -> "${value / 1_000_000L}M"
    value >= 1_000L             -> "${value / 1_000L}K"
    else                        -> value.toString()
}
