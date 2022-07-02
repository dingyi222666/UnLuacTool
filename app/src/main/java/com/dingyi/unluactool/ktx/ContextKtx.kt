package com.dingyi.unluactool.ktx

import com.dingyi.unluactool.MainApplication

fun getString(resId: Int): String = MainApplication.instance.getString(resId)

fun getString(resId: Int, vararg formatArgs: Any): String = MainApplication.instance.getString(resId, *formatArgs)