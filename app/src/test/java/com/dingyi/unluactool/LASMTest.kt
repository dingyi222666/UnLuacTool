package com.dingyi.unluactool

import com.dingyi.unluactool.engine.decompiler.LFunctionDecompiler
import com.dingyi.unluactool.engine.lasm.assemble.Assembler
import com.dingyi.unluactool.engine.lasm.disassemble.Disassembler
import org.junit.Assert.assertEquals
import org.junit.Test
import unluac.Configuration
import unluac.parse.BHeader
import unluac.parse.LFunction
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LASMTest {

    companion object {
        const val LUA_PATH = "G:\\dingyi\\Documents\\QQ_Data\\2187778735\\FileRecv\\androlua.luac"
    }


    @Test
    fun lasmTest1() {
        val path = LUA_PATH

        val lFunction = checkNotNull(
            fileToFunction(path, Configuration().apply {
                this.mode = Configuration.Mode.ASSEMBLE
                this.variable = Configuration.VariableMode.FINDER
            })
        )
        println(lFunction)

    }

    @Test
    fun lasmTest2() {
        val path = LUA_PATH

        val header = checkNotNull(fileToBHeader(path, Configuration().apply {
            this.mode = Configuration.Mode.ASSEMBLE
            this.variable = Configuration.VariableMode.DEFAULT
        }))

        val disassembler = Disassembler(header.main)
        val function = disassembler.disassemble()
        println(function.getDataWithChildFunctions())
        val assembler = Assembler(function)

        val string = assembler.assemble(function.childFunctions.get(1)).second.let {
            LFunctionDecompiler.decompile(it)
        }.decodeToString()
        println("-----------------------------------------------------")
        println(string)
    }


    private fun fileToBHeader(path: String, config: Configuration): BHeader? {
        return runCatching {
            val file = RandomAccessFile(path, "r")
            val buffer = ByteBuffer.allocate(file.length().toInt())
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            var len = file.length().toInt()
            val channel = file.channel
            while (len > 0) len -= channel.read(buffer)
            buffer.rewind()
            BHeader(buffer, config)
        }.getOrNull()
    }

    private fun fileToFunction(fn: String, config: Configuration): LFunction? {
        return fileToBHeader(fn, config)?.main
    }


}