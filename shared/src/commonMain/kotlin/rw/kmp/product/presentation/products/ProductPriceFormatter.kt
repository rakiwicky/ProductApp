package rw.kmp.product.presentation.products

import kotlin.math.abs
import kotlin.math.roundToLong

fun formatUsdPrice(price: Double): String {
    val cents = (price * 100).roundToLong()
    val dollars = abs(cents / 100)
    val fractionalCents = abs(cents % 100).toString().padStart(2, '0')
    val currencyPrefix = if (cents < 0) "-\$" else "\$"
    return "$currencyPrefix$dollars.$fractionalCents"
}
