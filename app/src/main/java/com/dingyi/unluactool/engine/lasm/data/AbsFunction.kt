package com.dingyi.unluactool.engine.lasm.data

interface AbsFunction<T> {

    fun addChildFunction(func: T)

    fun removeChildFunction(func: T)

    fun removeChildFunctionByName(name: String)

    var data: String

    val fullName: String

    val name: String
}