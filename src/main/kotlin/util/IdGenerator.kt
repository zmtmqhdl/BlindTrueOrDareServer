package com.example.presentation.util

import java.math.BigInteger
import java.util.UUID

fun IdGenerator(): String {
    return UUID.randomUUID()
        .toString()
        .replace("-", "")
        .let { BigInteger(it, 16) }
        .toString(62)
}