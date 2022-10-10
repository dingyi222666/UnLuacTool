package com.dingyi.unluactool.core.event

import com.dingyi.unluactool.core.event.internal.EventManagerImpl

class EventServiceRegistry {

    fun createEventManager(): EventManager = EventManagerImpl()
}