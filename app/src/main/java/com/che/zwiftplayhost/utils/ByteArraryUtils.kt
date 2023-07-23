package com.che.zwiftplayhost.utils

const val PREFIX = "0x"

fun ByteArray.toHexString(): String {
    val result = asUByteArray().joinToString(" $PREFIX", prefix = PREFIX, postfix = " ") { it.toString(16).uppercase().padStart(2, '0') }
    if (result != "$PREFIX ")
        return result
    return ""
}

fun ByteArray.startsWith(otherByteArray: ByteArray): Boolean {
    if (this.size < otherByteArray.size) return false
    for ((index, byte) in otherByteArray.withIndex()) {
        if (this[index] != byte) return false
    }
    return true
}