package com.dingyi.unluactool.engine.decompiler

interface Decompiler<T,R> {

    fun decompile(input:T):R

}