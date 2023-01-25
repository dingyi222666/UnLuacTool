package com.dingyi.unluactool.engine.lasm.assemble.unluac

import com.dingyi.unluactool.engine.lasm.assemble.AbstractLasmAssembler
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import unluac.Configuration
import unluac.assemble.Assembler
import java.io.ByteArrayInputStream
import java.io.OutputStream

class UnLuaCLasmAssembler : AbstractLasmAssembler {
    override fun assembleToStream(mainChunk: LASMChunk, output: OutputStream):Boolean {
        return kotlin.runCatching {
            Assembler(
                ByteArrayInputStream(mainChunk.getAllData().encodeToByteArray()),
                output
            ).assemble()
        }.isSuccess
    }

    override fun assembleToObject(mainChunk: LASMChunk): Any {
        val inputStream = ByteArrayInputStream(mainChunk.getAllData().encodeToByteArray())
        val assembler = Assembler(
            inputStream,
            null
        )
        val chunk = assembler.chunk

        val mainAssembleFunction = chunk.main

        return chunk.convertToFunction(mainAssembleFunction)
    }

    override fun assembleToObject(
        mainChunk: LASMChunk,
        targetFunction: LASMFunction
    ): Pair<Any, Any> {
        val inputStream = ByteArrayInputStream(mainChunk.getAllData().encodeToByteArray())
        val assembler = Assembler(
            inputStream,
            null
        )
        val chunk = assembler.chunk

        val mainAssembleFunction = chunk.main
        val part = targetFunction.fullName.split("/").toTypedArray()

        val currentASMFunction = mainAssembleFunction.getInnerChild(
            part, 1
        )

        val mainFunction = chunk.convertToFunction(mainAssembleFunction)

        val currentFunction = chunk.convertToFunction(currentASMFunction)
        mainFunction.header.config = Configuration().apply {
            variable = Configuration.VariableMode.FINDER
        }

        inputStream.close()
        return Pair(mainFunction, currentFunction)
    }
}