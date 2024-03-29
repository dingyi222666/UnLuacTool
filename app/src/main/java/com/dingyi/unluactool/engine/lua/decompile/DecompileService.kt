package com.dingyi.unluactool.engine.lua.decompile

import com.dingyi.unluactool.core.util.JsonConfigReader
import com.dingyi.unluactool.engine.service.BaseServiceContainer

class DecompileService : BaseServiceContainer<Decompiler>(), DecompilerGetter {

    override val globalConfigPath
        get() = "decompile-service.json"

    override fun getDecompilerByName(name: String): Decompiler? {
        return allService.find { it.name == name }
    }

    fun decompile(input: ByteArray, configuration: Any? = null): Any? {
        for (decompiler in allService) {
            val result = kotlin.runCatching {
                decompiler.decompile(input, configuration, this)
            }.onFailure {
                it.printStackTrace()
            }.getOrNull()
            if (result != null) {
                return result
            }
        }
        return null
    }

    fun decompileToSource(
        input: Any,
        configuration: Any?
    ): Any? {
        for (decompiler in allService) {
            val result = kotlin.runCatching {
                decompiler.decompileToSource(input, configuration, this)
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