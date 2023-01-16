package com.dingyi.unluactool.engine.lasm.assemble


import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import unluac.Configuration
import unluac.assemble.Assembler
import unluac.parse.LFunction
import java.io.ByteArrayInputStream
import java.io.OutputStream

class Assembler(
    val main: LASMChunk,
) {

    fun assemble(output: OutputStream) {
        Assembler(
            ByteArrayInputStream(main.getAllData().encodeToByteArray()),
            output
        ).assemble()

    }

    /**
     * @return the main function and current assemble  function
     */
    fun assemble(assembleFunction: LASMFunction): Pair<LFunction, LFunction> {
        val inputStream = ByteArrayInputStream(main.getAllData().encodeToByteArray())
        val assembler = Assembler(
            inputStream,
            null
        )
        val chunk = assembler.chunk

        val mainAssembleFunction = chunk.main
        val part = assembleFunction.fullName.split("/").toTypedArray()

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