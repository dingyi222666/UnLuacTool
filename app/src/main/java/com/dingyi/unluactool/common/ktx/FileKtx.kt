package com.dingyi.unluactool.common.ktx

import org.apache.commons.vfs2.FileObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

val ZIP_HEADER_BYTES_1 = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
val ZIP_HEADER_BYTES_2 = byteArrayOf(0x50, 0x4b, 0x05, 0x06)

fun File.isZipFile(): Boolean {
    return inputStream().use { input ->
        val header = ByteArray(4)
        input.read(header) //read 4 byte
        header.contentEquals(ZIP_HEADER_BYTES_1) || header.contentEquals(
            ZIP_HEADER_BYTES_2
        )
    }
}

fun <R> FileObject.outputStream(block: (OutputStream) -> R): R {
    return content.use {
        block(it.outputStream)
    }
}

fun <R> FileObject.inputStream(block: (InputStream) -> R): R {
    return content.use {
        block(it.inputStream)
    }
}


