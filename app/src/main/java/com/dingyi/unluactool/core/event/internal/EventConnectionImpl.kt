package com.dingyi.unluactool.core.event.internal

import com.dingyi.unluactool.core.event.EventConnection
import com.dingyi.unluactool.core.event.EventType

internal class EventConnectionImpl(
    private val eventManager: EventManagerImpl
) : EventConnection {


    private val eventTypeMap = mutableMapOf<EventType<Any>, Any>()

    override fun <L:Any> subscribe(topic: EventType<L>, handler: L) {
        return eventManager.subscribe(topic, handler)
    }

    override fun disconnect() {
        eventTypeMap.forEach { (t, u) ->
            eventManager.unsubscribe(t, u)
        }
        eventTypeMap.clear()
    }
}