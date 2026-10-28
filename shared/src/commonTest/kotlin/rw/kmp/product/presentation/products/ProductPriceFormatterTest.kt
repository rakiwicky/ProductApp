package rw.kmp.product.presentation.products

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductPriceFormatterTest {
    @Test
    fun formatsWholeDollarAmountWithTwoDecimals() {
        assertEquals("$12.00", formatUsdPrice(12.0))
    }

    @Test
    fun formatsFractionalAmountWithTwoDecimals() {
        assertEquals("$12.50", formatUsdPrice(12.5))
    }

    @Test
    fun formatsSmallAmountsWithLeadingZero() {
        assertEquals("$0.05", formatUsdPrice(0.05))
    }
}
