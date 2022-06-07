package com.dingyi.unluactool.network

import okhttp3.Response

suspend fun get(url:String) {

}

/**
 * 转换响应对象
 */
interface ResponseConvert<T> {
    suspend fun convert(response: Response):T
}

class JsonConvert<T>:ResponseConvert<T> {
    override suspend fun convert(response: Response): T {

    }
    companion object {

    }
}