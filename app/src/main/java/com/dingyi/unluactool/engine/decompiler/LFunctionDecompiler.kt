package com.dingyi.unluactool.engine.decompiler

import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import unluac.decompile.Output
import unluac.parse.LFunction


object LFunctionDecompiler : Decompiler<LFunction, ByteArray> {
    override fun decompile(input: LFunction): ByteArray {

        val d = unluac.decompile.Decompiler(input);
        val result = d.decompile();
        val provider = ByteArrayOutputProvider()
        val output = Output(provider)
        d.print(result, output);
        val data = provider.getBytes()
        provider.close()
        return data
    }
}