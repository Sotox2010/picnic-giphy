package com.jesussoto.android.giphy.api

import com.jesussoto.android.giphy.BuildConfig

import java.io.IOException

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AuthInterceptor(private val apiKey: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url().newBuilder()
                .addQueryParameter(WebService.PARAM_API_KEY, apiKey)
                .build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}
