package com.dingyi.unluactool.core.service

interface Service {

    fun <T> get(clazz: Class<T>): T

    fun <T> find(clazz: Class<T>): T?

}