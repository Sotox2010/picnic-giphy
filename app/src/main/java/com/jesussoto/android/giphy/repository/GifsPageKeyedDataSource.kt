package com.jesussoto.android.giphy.repository

import android.annotation.SuppressLint
import android.arch.paging.PageKeyedDataSource
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.api.WebService
import com.jesussoto.android.giphy.api.response.MultipleGifResponse
import com.jesussoto.android.giphy.model.GifObject
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class GifsPageKeyedDataSource constructor(
        private val service: WebService) : PageKeyedDataSource<Int, GifObject>() {

    // Keep a function reference for the retry event.
    private var retryAction: (() -> Unit)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState: BehaviorSubject<Resource.Status> = BehaviorSubject.create()

    val initialLoad: BehaviorSubject<Resource.Status> = BehaviorSubject.create()

    @SuppressLint("CheckResult")
    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, GifObject>) {
        val initialOffset = 0
        networkState.onNext(Resource.Status.LOADING)
        initialLoad.onNext(Resource.Status.LOADING)

        service.getTrendingGifs(params.requestedLoadSize, initialOffset)
                .map(MultipleGifResponse::data)
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        { this.handleLoadInitialSuccess(it, callback) },
                        { this.handleLoadInitialError(it, params, callback) }
                )
    }

    private fun handleLoadInitialSuccess(
            gifs: List<GifObject>, callback: LoadInitialCallback<Int, GifObject>) {

        callback.onResult(gifs, null, 25)
        networkState.onNext(Resource.Status.SUCCESS)
        initialLoad.onNext(Resource.Status.SUCCESS)
        retryAction = null
    }

    private fun handleLoadInitialError(
            t: Throwable, params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Int, GifObject>) {

        networkState.onNext(Resource.Status.ERROR)
        initialLoad.onNext(Resource.Status.ERROR)
        retryAction = { loadInitial(params, callback) }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, GifObject>) {

        // Intentionally ignored, since we only ever append to our initial load.
    }

    @SuppressLint("CheckResult")
    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, GifObject>) {
        networkState.onNext(Resource.Status.LOADING)
        service.getTrendingGifs(params.requestedLoadSize, params.key)
                .map(MultipleGifResponse::data)
                .subscribeOn(Schedulers.computation())
                .subscribe(
                        { handleLoadAfterSuccess(it, params, callback) },
                        { handleLoadAfterError(it, params, callback) }
                )
    }

    private fun handleLoadAfterSuccess(
            gifs: List<GifObject>, params: LoadParams<Int>,
            callback: LoadCallback<Int, GifObject>) {

        callback.onResult(gifs, params.key + params.requestedLoadSize)
        networkState.onNext(Resource.Status.SUCCESS)
        retryAction = null
    }

    private fun handleLoadAfterError(
            t: Throwable, params: LoadParams<Int>, callback: LoadCallback<Int, GifObject>) {

        retryAction = { loadAfter(params, callback) }
        networkState.onNext(Resource.Status.ERROR)
    }

    fun retryFailed() {
        val retry = retryAction
        retryAction = null
        if (retry != null) {
            Completable.fromAction(retry)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(Schedulers.computation())
                    .subscribe()
        }
    }
}
