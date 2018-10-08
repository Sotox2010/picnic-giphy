package com.jesussoto.android.giphy.repository

import android.arch.paging.DataSource

import com.jesussoto.android.giphy.api.WebService
import com.jesussoto.android.giphy.model.GifObject
import io.reactivex.subjects.BehaviorSubject

class GifsDataSourceFactory(
        private val service: WebService) : DataSource.Factory<Int, GifObject>() {

    val sourceSubject: BehaviorSubject<GifsPageKeyedDataSource> = BehaviorSubject.create()

    override fun create(): DataSource<Int, GifObject> {
        val source = GifsPageKeyedDataSource(service)
        sourceSubject.onNext(source)
        return source
    }
}
