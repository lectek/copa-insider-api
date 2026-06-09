package br.com.redemaisfarma.mobile.ui.util

fun formatCpf(input: String): String {
    val digits = input.filter(Char::isDigit).take(11)
    val builder = StringBuilder()
    digits.forEachIndexed { index, c ->
        builder.append(c)
        if (index == 2 || index == 5) builder.append('.')
        if (index == 8) builder.append('-')
    }
    return builder.toString()
}

fun formatTelefone(input: String): String {
    val digits = input.filter(Char::isDigit).take(11)
    val builder = StringBuilder()
    digits.forEachIndexed { index, c ->
        when (index) {
            0 -> builder.append('(').append(c)
            1 -> builder.append(c).append(") ")
            6 -> builder.append('-').append(c)
            else -> builder.append(c)
        }
    }
    return builder.toString()
}
