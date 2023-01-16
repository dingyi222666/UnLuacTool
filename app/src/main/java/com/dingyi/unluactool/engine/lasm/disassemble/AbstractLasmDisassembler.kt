package com.dingyi.unluactool.engine.lasm.disassemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk

interface AbstractLasmDisassembler {

    fun disassemble(input: Any): LASMChunk

    fun isSupportDisassemble(input: Any): Boolean
}