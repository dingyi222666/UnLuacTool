package com.dingyi.unluactool.core.event

interface EventManager {

    fun <T:Any> syncPublisher(eventType: EventType<T>): T

    fun <T:Any> subscribe(eventType: EventType<T>, target: T)

    fun <T:Any> clearListener(eventType: EventType<T>)

    fun connect(): EventConnection

    fun <T:Any> unsubscribe(eventType: EventType<T>, target: T)



    fun close()

}