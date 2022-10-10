package com.dingyi.unluactool.core.event

interface EventConnection {

    fun <T> subscribe(eventType: EventType<T>, target: T)

    fun close()
}