package com.dingyi.unluactool.core.event.internal

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventType
import com.google.gson.JsonObject
import com.google.gson.JsonParser

internal class RootEventManager : EventManagerImpl(null) {


    val selfConnection = connect()

    init {
        val preLoadJsonString = this.javaClass
            .getResourceAsStream("META-INF/global.json")
            ?.readBytes()
            ?.decodeToString()
            .toString()





        runCatching {
            val element = JsonParser.parseString(preLoadJsonString).asJsonObject
            element.entrySet().forEach { (listenerClassName, arrays) ->
                val listenerClass = Class.forName(listenerClassName)
                //类型擦除
                val targetEventType: EventType<Any> =
                    EventType.create(listenerClass) as EventType<Any>
                arrays.asJsonArray.forEach {
                    val targetClass = Class.forName(it.asString)

                    selfConnection.subscribe(targetEventType, targetClass.newInstance())
                }
            }
        }

    }

    override fun close(closeParent: Boolean) {
        selfConnection.disconnect()
        super.close(closeParent)
    }
}