package com.dingyi.unluactool.network


import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File

object SimpleHttp {

    private val okHttpClient = OkHttpClient()
    suspend fun <T> get(url: String, convert: ResponseConvert<T>): T? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()

            val call = okHttpClient.newCall(request)

            val response = call.execute()

            if (!isActive) {
                response.close()
                return@withContext null
            }
            response.use {
                convert.convert(response)
            }

        }


}
/**
 * 转换响应对象
 */
interface ResponseConvert<T> {
    suspend fun convert(response: Response): T?
}

suspend fun Response.readStringForResponse(): String? = withContext(Dispatchers.IO) {
    kotlin.runCatching {
        body?.string()
    }.getOrNull()
}

inline fun <reified T> createJsonConvert(): JsonConvert<T> {
    return JsonConvert(T::class.java)
}

class JsonConvert<T>(
    private val convertClass: Class<T>
) : ResponseConvert<T> {
    override suspend fun convert(response: Response): T? {
        return kotlin.runCatching {
            globalGson.fromJson(response.readStringForResponse(), convertClass)
        }.getOrNull()
    }

    companion object {
        private val globalGson = Gson()
    }
}