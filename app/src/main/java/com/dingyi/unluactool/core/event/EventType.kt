package com.dingyi.unluactool.core.event

import com.dingyi.unluactool.common.ktx.getJavaClass

class EventType<T> private constructor(
    val listenerClass: Class<T>
) {



    companion object {
        fun <T> create(clazz: Class<T>): EventType<T> {
            return EventType(clazz)
        }
        inline fun <reified T> create(): EventType<T> {
            return create(getJavaClass())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventType<*>

        if (listenerClass != other.listenerClass) return false

        return true
    }

    override fun hashCode(): Int {
        return listenerClass.hashCode()
    }


}

