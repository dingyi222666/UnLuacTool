package com.dingyi.unluactool.core.service

import androidx.annotation.Nullable
import java.lang.reflect.Type

interface ServiceProvider {

    /**
     * Locates a service instance of the given type. Returns null if this provider does not provide a service of this type.
     */
    @Nullable
    fun getService(serviceType: Type): Service?


}