package com.dingyi.unluactool.common.ktx

inline fun <reified T> getJavaClass(): Class<T> {
    return T::class.java
}