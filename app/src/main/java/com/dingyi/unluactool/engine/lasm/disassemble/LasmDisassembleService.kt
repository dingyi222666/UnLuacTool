package com.dingyi.unluactool.engine.lasm.disassemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.service.BaseServiceContainer

class LasmDisassembleService : BaseServiceContainer<AbstractLasmDisassembler>() {
    override val globalConfigPath = "lasm-disassemble-service.json"

    fun disassemble(input: Any): LASMChunk? {
        for (disassembler in allService) {
            if (!disassembler.isSupportDisassemble(input)) {
                continue
            }
            val result = kotlin.runCatching {
                disassembler.disassemble(input)
            }.getOrNull()

            if (result != null) {
                return result
            }
        }
        return null
    }
}