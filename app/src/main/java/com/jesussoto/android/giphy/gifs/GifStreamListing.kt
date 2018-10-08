package com.jesussoto.android.giphy.gifs

import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.model.GifObject
import io.reactivex.Observable

/**
 * Wrapper class that encapsulates all the data related to a paged stream of gifs, like the gif
 * stream itself, the network state, and retry action.
 */
class GifStreamListing(

    val pagedList: LiveData<PagedList<GifObject>>,

    val networkState: Observable<Resource.Status>,

    val initialLoadState: Observable<Resource.Status>,

    val retryAction: () -> Unit
)
