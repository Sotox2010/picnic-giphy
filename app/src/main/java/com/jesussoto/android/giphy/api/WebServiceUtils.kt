package com.jesussoto.android.giphy.api

import com.jesussoto.android.giphy.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object WebServiceUtils {

    val instance: WebService by lazy {
        val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(BuildConfig.GIPHY_API_KEY))

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(loggingInterceptor)
        }

        return@lazy Retrofit.Builder()
                .baseUrl(BuildConfig.GIPHY_API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(clientBuilder.build())
                .build()
                .create(WebService::class.java)
    }
}
