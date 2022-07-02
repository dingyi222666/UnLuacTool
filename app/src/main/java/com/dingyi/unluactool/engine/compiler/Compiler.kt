package com.dingyi.unluactool.engine.compiler

interface Compiler<T, R> {
    fun compile(t: T): R
}