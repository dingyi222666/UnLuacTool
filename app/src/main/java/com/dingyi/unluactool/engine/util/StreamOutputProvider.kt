package com.dingyi.unluactool.engine.util

import unluac.decompile.OutputProvider
import java.io.Closeable
import java.io.OutputStream

class StreamOutputProvider(
    private val out:OutputStream
    ): OutputProvider,Closeable {



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


    override fun close() {
        out.close();
    }
}