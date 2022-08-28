package com.dingyi.unluactool.common.ktx

import java.nio.ByteBuffer
import java.nio.ByteOrder


fun ByteArray.getIntAt(idx: Int) =
    ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .getInt(idx)

fun ByteArray.getDoubleAt(idx: Int) =
    ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .getDouble(idx)


fun ByteArray.getLongAt(idx: Int) =
    ByteBuffer.wrap(this)
        .order(ByteOrder.LITTLE_ENDIAN)
        .getLong(idx)


fun Int.toByteArray(): ByteArray =
    ByteBuffer.allocate(4)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putInt(this)
        .array()

fun Double.toByteArray(): ByteArray =
    ByteBuffer.allocate(8)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putDouble(this)
        .array()


fun Long.toByteArray(): ByteArray =
    ByteBuffer.allocate(8)
        .order(ByteOrder.LITTLE_ENDIAN)
        .putLong(this)
        .array()
