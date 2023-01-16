package com.dingyi.unluactool.engine.service

import com.dingyi.unluactool.core.util.JsonConfigReader
import com.dingyi.unluactool.engine.lua.decompile.Decompiler

abstract class BaseServiceContainer<T> {

    abstract val globalConfigPath:String

    protected val allService = mutableListOf<T>()

    init {
        readGlobalService()
    }

    private fun readGlobalService() {
        val jsonArray = JsonConfigReader.readConfig(globalConfigPath).asJsonArray

        jsonArray.forEach {
            allService.add(Class.forName(it.asString).newInstance() as T)
        }

    }

    fun addService(service:T) {
        allService.add(service)
    }

    fun removeDecompiler(service: T) {
       allService.remove(service)
    }

}