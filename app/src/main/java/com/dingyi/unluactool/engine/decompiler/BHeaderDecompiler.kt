package com.dingyi.unluactool.engine.decompiler

import unluac.Configuration
import unluac.parse.BHeader
import java.nio.ByteBuffer
import java.nio.ByteOrder

object BHeaderDecompiler : Decompiler<Pair<unluac.Configuration, ByteBuffer>, BHeader> {
    override fun decompile(input: Pair<unluac.Configuration, ByteBuffer>): BHeader {
        val (configuration, buffer) = input
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return BHeader(buffer, configuration)
    }
}