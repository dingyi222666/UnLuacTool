package com.dingyi.unluactool

import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.event.EventServiceRegistry
import com.dingyi.unluactool.core.event.EventType
import com.dingyi.unluactool.core.service.ServiceRegistryBuilder
import com.dingyi.unluactool.core.service.get
import org.junit.Test
import java.util.concurrent.ForkJoinPool

class EventManagerTest {

    fun interface Test2 {
        fun call(i: Int)
    }

    @Test
    fun eventTest1() {
        val serviceRegistry = ServiceRegistryBuilder.builder()
            .displayName("test")
            .provider(EventServiceRegistry())
            .build()

        val eventManager = serviceRegistry.get<EventManager>()

        val eventType = EventType.create<Test2>()

        val connect = eventManager.connect()

        connect.subscribe(eventType, Test2 { i ->
            println("args:${i}")
        })

        eventManager.syncPublisher(eventType)
            .call(666)

        Thread.sleep(100)
    }
}