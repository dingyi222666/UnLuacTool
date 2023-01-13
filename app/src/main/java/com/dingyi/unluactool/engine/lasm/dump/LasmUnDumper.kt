package com.dingyi.unluactool.engine.lasm.dump

import com.dingyi.unluactool.common.ktx.getIntAt
import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream

class LasmUnDumper {


    fun unDump(stream: InputStream): LASMChunk {

        unDumpHeader(stream)

        //name

        val name = readString(stream)

        //full name
        val fullName = readString(stream)

        //version
        val version = readString(stream)

        //data
        val data = readString(stream)


        val chunk = LASMChunk(data, version, name, fullName)

        val childSize = readInt(stream)

        for (i in 1..childSize) {
            unDumpFunction(stream, chunk)
            //chunk.addChildFunction(func)
        }

        stream.close()

        return chunk
    }

    private fun unDumpFunction(
        stream: InputStream,
        parent: AbsFunction<LASMFunction>
    ): LASMFunction {
        //name

        val name = readString(stream)

        //full name
        val fullName = readString(stream)

        //data
        val data = readString(stream)

        val func = LASMFunction(data, name, fullName, parent)

        val childSize = readInt(stream)

        for (i in 1..childSize) {
            unDumpFunction(stream, func)
        }

        return func
    }


    private fun readString(stream: InputStream): String {
        val arraySize = readInt(stream)

        val byteArray = readByteArray(stream, arraySize)

        val zipInputStream = GZIPInputStream(ByteArrayInputStream(byteArray))

        val builder = StringBuilder()

        zipInputStream.bufferedReader().use {
            val cArray = CharArray(1024)
            var len = it.read(cArray, 0, cArray.size)
            while (len > 0) {
                builder.append(cArray, 0, len)
                len = it.read(cArray, 0, cArray.size)
            }

        }

        return builder.toString()

    }

    private fun readInt(stream: InputStream): Int {
        return readByteArray(stream, 4).getIntAt(0)
    }

    private fun readByteArray(stream: InputStream, size: Int): ByteArray {
        val array = ByteArray(size)

        stream.read(array)

        return array
    }

    private fun unDumpHeader(stream: InputStream) {
        val array = readByteArray(stream, LasmDumper.lasmHeader.size)

        if (!array.contentEquals(LasmDumper.lasmHeader)) {
            error("Does not match the header bytecode")
        }


    }
}