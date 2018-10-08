package com.jesussoto.android.giphy.gifs

import android.arch.lifecycle.ViewModel
import com.jesussoto.android.giphy.repository.GifRepository

class GifStreamViewModel(repository: GifRepository) : ViewModel() {

    private val gifListing = repository.loadTrendingGifStream()

    val pagedGifList = gifListing.pagedList

    val networkState = gifListing.networkState

    val initialLoadState = gifListing.initialLoadState

    /**
     * Retries the last failed data fetch.
     */
    fun retryLastFetch() {
        gifListing.retryAction.invoke()
    }
}
