package com.example.presentation.util

import java.security.SecureRandom

fun idGenerator(): String {
    val stringBuilder = StringBuilder(12)
    repeat(12) {
        stringBuilder.append(SecureRandom().nextInt(10))
    }
    return stringBuilder.toString()
}