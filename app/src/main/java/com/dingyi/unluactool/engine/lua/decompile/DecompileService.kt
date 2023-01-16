package com.dingyi.unluactool.engine.lua.decompile

import com.dingyi.unluactool.core.util.JsonConfigReader

class DecompileService : DecompilerGetter {

    private val allDecompiler = mutableListOf<Decompiler>()

    init {
        readGlobalDecompileService()
    }

    private fun readGlobalDecompileService() {
        val jsonArray = JsonConfigReader.readConfig("decompile-service.json").asJsonArray

        jsonArray.forEach {
            addDecompiler(Class.forName(it.asString).newInstance() as Decompiler)
        }

    }

    fun addDecompiler(decompiler: Decompiler) {
        allDecompiler.add(decompiler)
    }

    fun removeDecompiler(decompiler: Decompiler) {
        allDecompiler.remove(decompiler)
    }

    override fun getDecompilerByName(name: String): Decompiler? {
        return allDecompiler.find { it.name == name }
    }

    fun decompile(input: ByteArray, configuration: Any?): Any? {
        for (decompiler in allDecompiler) {
            val result = kotlin.runCatching {
                decompiler.decompile(input, configuration, this)
            }.getOrNull()
            if (result != null) {
                return result
            }
        }
        return null
    }
}