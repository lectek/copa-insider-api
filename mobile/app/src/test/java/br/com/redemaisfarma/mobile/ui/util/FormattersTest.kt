package br.com.redemaisfarma.mobile.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {
    @Test
    fun formatCpf_appliesMask() {
        val result = formatCpf("12345678901")
        assertEquals("123.456.789-01", result)
    }

    @Test
    fun formatCpf_stripsExtraDigits() {
        val result = formatCpf("12345678901234")
        assertEquals("123.456.789-01", result)
    }

    @Test
    fun formatTelefone_appliesMask() {
        val result = formatTelefone("11987654321")
        assertEquals("(11) 98765-4321", result)
    }
}
