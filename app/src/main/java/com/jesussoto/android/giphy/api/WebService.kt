package com.jesussoto.android.giphy.api

import com.jesussoto.android.giphy.api.response.MultipleGifResponse
import com.jesussoto.android.giphy.api.response.SingleGifResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface WebService {

    @GET(PATH_TRENDING)
    fun getTrendingGifs(
            @Query(PARAM_LIMIT) limit: Int,
            @Query(PARAM_OFFSET) offset: Int): Single<MultipleGifResponse>

    @GET(PATH_RANDOM)
    fun getRandomGif(): Single<SingleGifResponse>

    companion object {

        /**
         * Paths for the Giphy web service.
         */
        private const val PATH_TRENDING = "v1/gifs/trending"
        private const val PATH_RANDOM = "/v1/gifs/random"


        /**
         * Query params for the web service.
         */
        const val PARAM_API_KEY = "api_key"
        const val PARAM_LIMIT = "limit"
        const val PARAM_OFFSET = "offset"
    }
}
