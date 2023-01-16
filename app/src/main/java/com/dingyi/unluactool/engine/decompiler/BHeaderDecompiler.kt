package com.dingyi.unluactool.engine.decompiler

import unluac.Configuration
import unluac.parse.BHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BHeaderDecompiler : Decompiler<Pair<unluac.Configuration, ByteBuffer>, unluac.parse.BHeader> {
    override fun decompile(input: Pair<unluac.Configuration, ByteBuffer>): unluac.parse.BHeader {
        val (configuration, buffer) = input
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return unluac.parse.BHeader(buffer, configuration)
    }
}