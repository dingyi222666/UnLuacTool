package com.dingyi.unluactool.core.event.internal

import com.dingyi.unluactool.core.event.EventType
import java.lang.reflect.Method

data class Event(
    val targetMethod: Method,
    val eventType: EventType<*>,
    val args: Array<Any?>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (targetMethod != other.targetMethod) return false
        if (eventType != other.eventType) return false
        if (args != null) {
            if (other.args == null) return false
            if (!args.contentEquals(other.args)) return false
        } else if (other.args != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = targetMethod.hashCode()
        result = 31 * result + eventType.hashCode()
        result = 31 * result + (args?.contentHashCode() ?: 0)
        return result
    }

    internal fun execute(target: Any?) {
        targetMethod
            .invoke(target, *(args ?: EMPTY_ARRAY))
    }

    companion object {
        private val EMPTY_ARRAY = arrayOfNulls<Any>(0)
    }
}