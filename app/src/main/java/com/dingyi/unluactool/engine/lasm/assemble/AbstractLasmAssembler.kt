package com.dingyi.unluactool.engine.lasm.assemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import java.io.OutputStream

interface AbstractLasmAssembler {
    fun assembleToStream(mainChunk: LASMChunk, output: OutputStream):Boolean

    fun assembleToObject(mainChunk: LASMChunk): Any

    fun assembleToObject(mainChunk: LASMChunk, targetFunction:LASMFunction): Pair<Any,Any>

}