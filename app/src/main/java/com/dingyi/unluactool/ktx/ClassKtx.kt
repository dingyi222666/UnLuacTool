package com.dingyi.unluactool.ktx

inline fun <reified T> getJavaClass(): Class<T> {
    return T::class.java
}