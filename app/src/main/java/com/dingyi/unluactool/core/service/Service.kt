package com.dingyi.unluactool.core.service

/**
 * Wraps a single service instance. Implementations must be thread safe.
 */
interface Service {
    fun get(): Any

    fun getDisplayName(): String

}