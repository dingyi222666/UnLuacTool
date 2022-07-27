package com.dingyi.unluactool.core.service

/**
 * Represents a source of services.
 */
internal interface ContainsServices {
    fun asProvider(): ServiceProvider
}