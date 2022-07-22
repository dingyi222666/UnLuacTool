package com.dingyi.unluactool.core.service

interface ServiceProvider {

    fun getType():Class<*>

    fun <T> get():T

}