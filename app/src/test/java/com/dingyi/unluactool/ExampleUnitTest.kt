package com.dingyi.unluactool

import org.junit.Assert.assertEquals
import org.junit.Test
import unluac.Configuration
import unluac.parse.BHeader
import unluac.parse.LFunction
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun lasmTest1() {
        val path = "C:\\Users\\dingyi\\Nox_share\\ImageShare\\c.lua"

        val lFunction = checkNotNull(
            fileToFunction(path, Configuration().apply {
                this.mode = Configuration.Mode.ASSEMBLE
                this.variable = Configuration.VariableMode.FINDER
            })
        )
        println(lFunction)

    }


    private fun fileToFunction(fn: String, config: Configuration): LFunction? {
        var file: RandomAccessFile? = null
        return try {
            file = RandomAccessFile(fn, "r")
            val buffer = ByteBuffer.allocate(file.length().toInt())
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            var len = file.length().toInt()
            val `in` = file.channel
            while (len > 0) len -= `in`.read(buffer)
            buffer.rewind()
            val header = BHeader(buffer, config)
            header.main
        } finally {
            file?.close()
        }
    }


}