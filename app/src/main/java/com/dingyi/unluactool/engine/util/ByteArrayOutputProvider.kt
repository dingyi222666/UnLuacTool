package com.dingyi.unluactool.engine.util

import unluac.decompile.OutputProvider
import java.io.ByteArrayOutputStream

class ByteArrayOutputProvider : unluac.decompile.OutputProvider {

    private var out = ByteArrayOutputStream()

    private val eol = System.lineSeparator();

    override fun print(s: String) {
        for (element in s) {
            val c = element.code
            check(!(c < 0 || c > 255))
            print(c.toByte())
        }
    }

    override fun print(b: Byte) {
        out.write(b.toInt())
    }

    override fun println() {
        print(eol)
    }

    fun getBytes(): ByteArray {
        return out.toByteArray()
    }

    fun reset() {
        out.reset();
      //  out = ByteArrayOutputStream()
    }

    fun close() {
        out.reset()
        out.close();
    }
}