package com.dingyi.unluactool.common.ktx

import java.io.File

val ZIP_HEADER_BYTES_1 = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
val ZIP_HEADER_BYTES_2 = byteArrayOf(0x50, 0x4b, 0x05, 0x06)

fun File.isZipFile():Boolean {
    return inputStream().use { input ->
        val header = ByteArray(4)
        input.read(header) //read 4 byte
        header.contentEquals(ZIP_HEADER_BYTES_1) || header.contentEquals(
            ZIP_HEADER_BYTES_2
        )
    }
}