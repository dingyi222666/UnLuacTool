package com.dingyi.unluactool.core.event.internal

import android.os.Handler
import android.os.Looper
import com.dingyi.unluactool.core.event.EventConnection
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.ui.editor.edit.EditFragment
import java.lang.reflect.Proxy
import java.util.Arrays
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

open class EventManagerImpl(private val parent: EventManagerImpl?) : EventManager {

    private val receivers = mutableMapOf<EventType<*>, MutableSet<Any>>()

    private val lock = ReentrantReadWriteLock()

    private val children = mutableListOf<EventManagerImpl>()

    private val stickyEventCaches = mutableMapOf<EventType<*>, Event>()

    private val publisherCaches = mutableMapOf<EventType<*>, Any>()

    private val handler = Handler(Looper.getMainLooper())

    private var isRunOnUiThread = true

    constructor() : this(null)

    init {
        parent?.lock?.read {
            parent.children.add(this)
        }
    }

    override fun dispatchEventOnUiThread() {
        isRunOnUiThread = true
    }

    override fun dispatchEventOnThreadPool() {
        isRunOnUiThread = false
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
        subscribeInternal(eventType, target, true)
    }

    override fun <T : Any> subscribe(eventType: EventType<T>, target: T, stickyEvent: Boolean) {
        subscribeInternal(eventType, target, stickyEvent)
    }

    private fun <T : Any> subscribeInternal(
        eventType: EventType<T>,
        target: T,
        stickyEvent: Boolean
    ) {
        val receivers = lock.read { receivers.getOrDefault(eventType, mutableSetOf()) }

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

        // sticky event support

        if (!stickyEvent) {
            return
        }

        lock.read {
            stickyEventCaches[eventType]
        }?.execute(target)
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
     * Get root Manager
     */
    fun getRootManager(): EventManager {
        return parent?.getRootManager() ?: this
    }

    private fun dispatchEventInternal(event: Event) {
        println("event:$event")
        val receivers = lock.read {
            stickyEventCaches[event.eventType] = event
            this.receivers[event.eventType]
        }

        println("receivers: $receivers")

        lock.read {
            receivers?.forEach {
                println("target:$it")
                executeEvent(event, it)
            }
        }

        lock.read {
            for (sub in children) {
                sub.dispatchEvent(event)
            }
        }
    }

    private fun executeEvent(event: Event, target: Any) {
        if (isRunOnUiThread) {
            event.execute(target)
        } else {
            handler.post {
                event.execute(target)
            }
        }
    }

    private fun dispatchEvent(event: Event) {
        if (isRunOnUiThread) {
            dispatchEventInternal(event)
        } else {
            ForkJoinPool.commonPool().submit {
                dispatchEventInternal(event)
            }
        }
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
        stickyEventCaches.clear()
    }

}