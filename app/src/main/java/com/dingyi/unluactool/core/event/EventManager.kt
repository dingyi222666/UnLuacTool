package com.dingyi.unluactool.core.event

interface EventManager {

    fun <T> syncPublisher(eventType: EventType<T>): T

    fun <T> subscribe(eventType: EventType<T>, target: T)

    fun <T> clearListener(eventType: EventType<T>)

    fun connect(): EventConnection

    fun <T> unsubscribe(eventType: EventType<T>, target: T)
}