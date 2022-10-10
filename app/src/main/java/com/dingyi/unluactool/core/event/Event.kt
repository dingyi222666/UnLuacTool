package com.dingyi.unluactool.core.event

import java.lang.reflect.Method

data class Event(
    private val targetMethod: Method,
    private val callClass: Class<*>,
    private val args: Array<Any?>?

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (targetMethod != other.targetMethod) return false
        if (callClass != other.callClass) return false
        if (args != null) {
            if (other.args == null) return false
            if (!args.contentEquals(other.args)) return false
        } else if (other.args != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = targetMethod.hashCode()
        result = 31 * result + callClass.hashCode()
        result = 31 * result + (args?.contentHashCode() ?: 0)
        return result
    }
}