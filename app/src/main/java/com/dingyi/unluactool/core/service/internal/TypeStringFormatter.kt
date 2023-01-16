package com.dingyi.unluactool.core.service.internal

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


internal object TypeStringFormatter {
    fun format(type: Type): String {
        if (type is Class<*>) {
            val aClass = type
            val enclosingClass = aClass.enclosingClass
            return if (enclosingClass != null) {
                format(enclosingClass) + "$" + aClass.simpleName
            } else {
                aClass.simpleName
            }
        } else if (type is ParameterizedType) {
            val builder = StringBuilder()
            builder.append(format(type.rawType))
            builder.append("<")
            for (i in type.actualTypeArguments.indices) {
                val typeParam = type.actualTypeArguments[i]
                if (i > 0) {
                    builder.append(", ")
                }
                builder.append(format(typeParam))
            }
            builder.append(">")
            return builder.toString()
        }
        return type.toString()
    }
}