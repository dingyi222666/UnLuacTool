package com.dingyi.unluactool.core.event

import com.dingyi.unluactool.core.event.internal.EventManagerImpl
import com.dingyi.unluactool.core.event.internal.RootEventManager

class EventServiceRegistry {

    fun createEventManager(): EventManager = RootEventManager()
}