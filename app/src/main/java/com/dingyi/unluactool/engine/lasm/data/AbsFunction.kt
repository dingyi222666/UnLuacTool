package com.dingyi.unluactool.engine.lasm.data

interface AbsFunction<T> {

    fun addChildFunction(func:T)

    fun removeChildFunction(func:T)

    fun removeChildFunctionByName(name:String)
}