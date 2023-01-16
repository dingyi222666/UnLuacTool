package com.dingyi.unluactool.engine.lasm.assemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import java.io.OutputStream

interface AbstractLasmAssembler {
    fun assemble(mainChunk: LASMChunk, output: OutputStream):Boolean

    fun assemble(mainChunk: LASMChunk): Any

}