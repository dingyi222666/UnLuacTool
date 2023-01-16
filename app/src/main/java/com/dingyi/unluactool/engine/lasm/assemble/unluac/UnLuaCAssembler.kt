package com.dingyi.unluactool.engine.lasm.assemble.unluac

import com.dingyi.unluactool.engine.lasm.assemble.AbstractLasmAssembler
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import unluac.assemble.Assembler
import java.io.ByteArrayInputStream
import java.io.OutputStream

class UnLuaCAssembler: AbstractLasmAssembler {
    override fun assemble(mainChunk: LASMChunk, output: OutputStream) {
        Assembler(
            ByteArrayInputStream(mainChunk.getAllData().encodeToByteArray()),
            output
        ).assemble()
    }
}