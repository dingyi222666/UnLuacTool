package com.dingyi.unluactool.core.event

import java.lang.reflect.Method

data class Event(
    private val targetMethod: Method,

)