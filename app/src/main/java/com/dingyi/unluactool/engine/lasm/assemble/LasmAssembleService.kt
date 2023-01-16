package com.dingyi.unluactool.engine.lasm.assemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.disassemble.AbstractLasmDisassembler
import com.dingyi.unluactool.engine.service.BaseServiceContainer
import java.io.OutputStream

class LasmAssembleService : BaseServiceContainer<AbstractLasmAssembler>() {
    override val globalConfigPath
        get() = "lasm-assemble-service.json"


    fun assemble(mainChunk: LASMChunk, output: OutputStream): Boolean {
        for (assembler in allService) {

            val result = kotlin.runCatching {
                assembler.assemble(mainChunk, output)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()

            if (result != null) {
                return result
            }
        }
        return false
    }

    fun assemble(mainChunk: LASMChunk): Any? {
        for (assembler in allService) {

            val result = kotlin.runCatching {
                assembler.assemble(mainChunk)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()

            if (result != null) {
                return result
            }
        }
        return null
    }


}