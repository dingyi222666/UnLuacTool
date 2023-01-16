package com.dingyi.unluactool.engine.lua.decompile

interface Decompiler<C : Any> {

    var configuration: C

    fun decompile(input: ByteArray): Any
}