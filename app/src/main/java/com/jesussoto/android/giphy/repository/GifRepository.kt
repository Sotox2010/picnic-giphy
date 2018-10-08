package com.jesussoto.android.giphy.repository

import android.annotation.SuppressLint
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.util.Log
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.api.WebService
import com.jesussoto.android.giphy.api.WebServiceUtils
import com.jesussoto.android.giphy.api.response.SingleGifResponse
import com.jesussoto.android.giphy.gifs.GifStreamListing
import com.jesussoto.android.giphy.model.GifObject
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.Executors

class GifRepository(
        // Web service to fetch the data from.
        private val service: WebService) {

    /**
     * Builds paged trending gifs
     *
     * @return [GifStreamListing] encapsulating all information about the gif stream.
     */
    fun loadTrendingGifStream(): GifStreamListing {
            val factory = GifsDataSourceFactory(service)
            val pagedListConfig = PagedList.Config.Builder()
                    .setEnablePlaceholders(false)
                    .setInitialLoadSizeHint(2 * PAGE_SIZE)
                    .setPageSize(PAGE_SIZE)
                    .build()

            val pagedList = LivePagedListBuilder(factory, pagedListConfig)
                    .setFetchExecutor(Executors.newFixedThreadPool(3))
                    .build()

            return GifStreamListing(
                    pagedList = pagedList,
                    networkState = factory.sourceSubject.switchMap { it.networkState },
                    initialLoadState = factory.sourceSubject.switchMap { it.initialLoad },
                    retryAction = {
                        factory.sourceSubject.value?.retryFailed()
                    }
            )
        }

            /**
     *
     *
     * @return [Resource] encapsulating all information about the random gif.
     */
    @SuppressLint("CheckResult")
    fun loadRandomGif(): Observable<Resource<GifObject>> {
        Log.d("GifRepository", "loadRandomGif")
        val result: BehaviorSubject<Resource<GifObject>> = BehaviorSubject.create()
        result.onNext(Resource.loading(null))

        service.getRandomGif()
                .map(SingleGifResponse::data)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .subscribe(
                        { result.onNext(Resource.success(it)) },
                        { result.onNext(Resource.error(it, null)) }
                )

        return result
    }

    companion object {

        /**
         * Default page size.
         */
        private const val PAGE_SIZE = 25

        /**
         * Get shared instance using the singleton pattern. By default, Kotlin synchronizes lazy
         * calls using the double-check locking pattern, so getting this instance is thread safe.
         *
         * @return the shared instance of [GifRepository].
         */
        val instance: GifRepository by lazy {
            return@lazy GifRepository(WebServiceUtils.instance)
        }
    }
}

