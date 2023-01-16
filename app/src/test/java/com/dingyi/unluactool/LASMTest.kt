package com.dingyi.unluactool

import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler2
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmDumper
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmUnDumper
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import org.junit.Test
import java.io.ByteArrayInputStream
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
            fileToFunction(path, unluac.Configuration().apply {
                this.mode = unluac.Configuration.Mode.ASSEMBLE
                this.variable = unluac.Configuration.VariableMode.FINDER
            })
        )
        println(lFunction)

    }

    @Test
    fun lasmTest2() {
        val path = LUA_PATH

        val header = checkNotNull(fileToBHeader(path, unluac.Configuration().apply {
            this.mode = unluac.Configuration.Mode.ASSEMBLE
            this.variable = unluac.Configuration.VariableMode.DEFAULT
        }))

        /*val disassembler = Disassembler(header.main)
        val function = disassembler.disassemble()
        println(function.getDataWithChildFunctions())
        val assembler = Assembler(function)

        val string = assembler.assemble(function.childFunctions.get(1)).second.let {
            LFunctionDecompiler.decompile(it)
        }.decodeToString()
        println("-----------------------------------------------------")
        println(string)*/
    }


    private fun println() {
        println("-----------------------------------------------------")
    }

    @Test
    fun lasmTest3() {
        val path = LUA_PATH

        val header = checkNotNull(fileToBHeader(path, unluac.Configuration().apply {
            this.mode = unluac.Configuration.Mode.ASSEMBLE
            this.variable = unluac.Configuration.VariableMode.DEFAULT
        }))

        val chunk = LasmDisassembler2(header.main).decompile()


        println(chunk.getAllData())

        println()


        val provider = ByteArrayOutputProvider()

        val output = unluac.decompile.Output(provider)

        val dumper = LasmDumper(output, chunk)

        dumper.dump()

        val bytes = provider.getBytes()

        println(String(bytes))

        provider.close()


        println()

        val unDumper = LasmUnDumper()

        val unDumperChunk = unDumper.unDump(ByteArrayInputStream(bytes))

        println(unDumperChunk.getAllData() == chunk.getAllData())

    }


    /**
     * 把文本设置到剪贴板（复制）
     */


    private fun fileToBHeader(path: String, config: unluac.Configuration): unluac.parse.BHeader? {
        return runCatching {
            val file = RandomAccessFile(path, "r")
            val buffer = ByteBuffer.allocate(file.length().toInt())
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            var len = file.length().toInt()
            val channel = file.channel
            while (len > 0) len -= channel.read(buffer)
            buffer.rewind()
            unluac.parse.BHeader(buffer, config)
        }.getOrNull()
    }

    private fun fileToFunction(fn: String, config: unluac.Configuration): unluac.parse.LFunction? {
        return fileToBHeader(fn, config)?.main
    }


}