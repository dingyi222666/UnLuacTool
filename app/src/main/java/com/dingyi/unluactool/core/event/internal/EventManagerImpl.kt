package com.dingyi.unluactool.core.event.internal

import com.dingyi.unluactool.core.event.EventConnection
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.event.EventType
import java.lang.reflect.Proxy
import java.util.Arrays
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

open class EventManagerImpl(private val parent: EventManagerImpl?) : EventManager {

    private val receivers = mutableMapOf<EventType<*>, MutableList<Any>>()
    private val lock = ReentrantReadWriteLock()

    private val children = mutableListOf<EventManagerImpl>()


    private val publisherCaches = mutableMapOf<EventType<*>, Any>()

    constructor() : this(null)

    init {
        parent?.children?.add(this)
    }

    override fun <T : Any> syncPublisher(eventType: EventType<T>): T {
        return lock.read {
            publisherCaches[eventType] as T?
        } ?: createPublisher(eventType).apply {
            lock.write {
                publisherCaches[eventType] = this
            }
        }
    }

    private fun <T : Any> createPublisher(eventType: EventType<T>): T {
        //1. get event type
        val eventClass = eventType.listenerClass

        //2. create new proxy
        val instance = Proxy.newProxyInstance(
            this.javaClass.classLoader,
            arrayOf(eventClass)
        ) { _, method, args ->
            val event = Event(targetMethod = method, eventType = eventType, args = args)
            dispatchEvent(event)
        }

        return instance as T

    }

    override fun <T : Any> subscribe(eventType: EventType<T>, target: T) {


        val receivers = lock.read { receivers.getOrDefault(eventType, mutableListOf()) }

        lock.write {
            receivers.add(target)
        }

        val needToPut = lock.read {
            this.receivers[eventType] == null
        }

        if (needToPut) {
            lock.write {
                this.receivers.put(eventType, receivers)
            }
        }


    }

    override fun <T : Any> clearListener(eventType: EventType<T>) {
        lock.write {
            receivers[eventType]?.clear()
        }
    }

    override fun connect(): EventConnection {
        return EventConnectionImpl(this)
    }

    override fun <T : Any> unsubscribe(eventType: EventType<T>, target: T) {
        val list = lock.read { receivers[eventType] }
        lock.write {
            list?.remove(target)
        }
    }


    fun getParent(): EventManager? = parent

    /**
     * Get root node
     */
    fun getRootManager(): EventManager {
        return parent?.getRootManager() ?: this
    }

    private fun dispatchEvent(event: Event) {
        /* ForkJoinPool.commonPool().submit {*/
        val receivers = lock.read {
            this.receivers[event.eventType]
        }

        receivers?.forEach {
            event.targetMethod
                .invoke(it, *(event.args ?: EMPTY_ARRAY))
        }
        /*}*/
    }


    override fun close(closeParent: Boolean) {
        if (parent != null && closeParent) {
            parent.close(true)
        } else {
            doClose()
        }
    }

    private fun doClose() {
        receivers.clear()
        children.forEach {
            it.doClose()
        }
        children.clear()
    }

    companion object {
        private val EMPTY_ARRAY = arrayOfNulls<Any>(0)
    }
}