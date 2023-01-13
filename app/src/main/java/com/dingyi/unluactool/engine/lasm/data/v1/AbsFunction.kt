package com.dingyi.unluactool.engine.lasm.data.v1

interface AbsFunction<T> {

    val childFunctions:MutableList<T>

    fun addChildFunction(func: T)

    fun removeChildFunction(func: T)

    fun removeChildFunctionByName(name: String)

    fun hasChildFunction(func:T):Boolean

    fun resolveFunction(path:String): LASMFunction?

    fun asFunction():T

    var data: String

    val fullName: String

    val name: String
}