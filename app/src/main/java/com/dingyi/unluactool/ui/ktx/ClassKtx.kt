package com.dingyi.unluactool.ui.ktx

inline fun <reified T> getJavaClass(): Class<T> {
    return T::class.java
}