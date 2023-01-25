package com.dingyi.unluactool.engine.lua.decompile.unluac

import com.dingyi.unluactool.engine.lua.decompile.Decompiler
import com.dingyi.unluactool.engine.lua.decompile.DecompilerGetter
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import unluac.Configuration
import unluac.decompile.Output
import unluac.parse.BHeader
import unluac.parse.LFunction
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UnLuaCDefaultDecompiler : Decompiler {

    override val name = "UnLuaCDecompiler"

    override fun decompile(
        input: ByteArray,
        configuration: Any?,
        decompilerGetter: DecompilerGetter
    ): Any {
        val buffer = ByteBuffer.wrap(input)
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        val unLuacConfig = (configuration as Configuration?) ?: Configuration().apply {
            rawstring = true
            mode = Configuration.Mode.DECOMPILE
            variable = Configuration.VariableMode.FINDER
        }
        return BHeader(buffer, unLuacConfig)
    }

    override fun decompileToSource(
        input: Any,
        configuration: Any?,
        decompilerGetter: DecompilerGetter
    ): Any {
        return if (input is LFunction)
            decompileToSourceInternal(input)
        else
            decompileToSourceInternal(
                (decompile(input as ByteArray, configuration, decompilerGetter) as BHeader).main
            )
    }

    private fun decompileToSourceInternal(function: LFunction): Any {
        val d = unluac.decompile.Decompiler(function);
        val result = d.decompile();
        val provider = ByteArrayOutputProvider()
        val output = Output(provider)
        d.print(result, output);
        val data = provider.getBytes()
        provider.close()
        return data.decodeToString()
    }
}