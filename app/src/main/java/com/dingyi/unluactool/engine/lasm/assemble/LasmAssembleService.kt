package com.dingyi.unluactool.engine.lasm.assemble

import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import com.dingyi.unluactool.engine.service.BaseServiceContainer
import java.io.OutputStream

class LasmAssembleService : BaseServiceContainer<AbstractLasmAssembler>() {
    override val globalConfigPath
        get() = "lasm-assemble-service.json"


    fun assembleToStream(mainChunk: LASMChunk, output: OutputStream): Boolean {
        for (assembler in allService) {

            val result = kotlin.runCatching {
                assembler.assembleToStream(mainChunk, output)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()

            if (result != null) {
                return result
            }
        }
        return false
    }

    fun assembleToObject(mainChunk: LASMChunk): Any? {
        for (assembler in allService) {
            val result = kotlin.runCatching {
                assembler.assembleToObject(mainChunk)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()

            if (result != null) {
                return result
            }
        }
        return null
    }

    fun assembleToObject(mainChunk: LASMChunk, targetFunction: LASMFunction): Pair<Any, Any>? {
        for (assembler in allService) {
            val result = kotlin.runCatching {
                assembler.assembleToObject(mainChunk, targetFunction)
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