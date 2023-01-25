package com.dingyi.unluactool.engine.lua.decompile

interface Decompiler {

    val name: String


    fun decompile(input: ByteArray, configuration: Any?,decompilerGetter: DecompilerGetter): Any

    fun decompileToSource(input:Any,configuration: Any?,decompilerGetter: DecompilerGetter):Any
}

interface DecompilerGetter {
    fun getDecompilerByName(name: String): Decompiler?
}