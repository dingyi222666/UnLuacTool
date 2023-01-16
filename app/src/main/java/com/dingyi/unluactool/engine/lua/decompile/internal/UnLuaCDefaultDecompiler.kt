package com.dingyi.unluactool.engine.lua.decompile.internal

import com.dingyi.unluactool.engine.lua.decompile.Decompiler
import com.dingyi.unluactool.engine.lua.decompile.DecompilerGetter
import unluac.Configuration
import unluac.parse.BHeader
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
            mode = unluac.Configuration.Mode.DECOMPILE
            variable = unluac.Configuration.VariableMode.FINDER
        }
        return BHeader(buffer, configuration)
    }
}