package com.dingyi.unluactool.core.event.internal

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.core.util.JsonConfigReader
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal class RootEventManager : EventManagerImpl(null) {


    private val selfConnection = connect()

    init {
        runCatching {
            val element = JsonConfigReader.readConfig("event-global.json").asJsonObject
            element.getAsJsonObject("extensions").entrySet()
                .forEach { (listenerClassName, arrays) ->
                    val listenerClass = Class.forName(listenerClassName)
                    //类型擦除
                    val targetEventType: EventType<Any> =
                        EventType.create(listenerClass) as EventType<Any>

                    val serviceRegistry = MainApplication.instance.globalServiceRegistry

                    arrays.asJsonArray.forEach {
                        val targetClass = Class.forName(it.asString)

                        val instance =
                            serviceRegistry.find(targetClass) ?: targetClass.newInstance()

                        selfConnection.subscribe(targetEventType, instance)
                    }
                }
        }.onFailure {
            it.printStackTrace()
        }
    }

    override fun close(closeParent: Boolean) {
        selfConnection.disconnect()
        super.close(closeParent)
    }
}