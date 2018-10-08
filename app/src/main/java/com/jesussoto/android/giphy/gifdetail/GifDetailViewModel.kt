package com.jesussoto.android.giphy.gifdetail

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.model.GifObject
import com.jesussoto.android.giphy.repository.GifRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class GifDetailViewModel(private val repository: GifRepository) : ViewModel() {

    private var isFirstRandomLoad: Boolean = true

    private var active: Boolean = true

    val randomGifState: BehaviorSubject<Resource<GifObject>> = BehaviorSubject.create()


    init {
        initRandomGifLoader()
    }

    // @SuppressLint("CheckResult")
    fun generateRandomGif(): Observable<Resource<GifObject>> {
        var initialDelay = 10L
        if (isFirstRandomLoad) {
            initialDelay = 0L
            isFirstRandomLoad = false
        }

        return Observable.interval(initialDelay, 10, TimeUnit.SECONDS)
                .switchMap { repository.loadRandomGif() }
    }

    @SuppressLint("CheckResult")
// @SuppressLint("CheckResult")
    fun initRandomGifLoader() {
        Observable.interval(0, 10, TimeUnit.SECONDS)
                .switchMap { if (active) repository.loadRandomGif() else Observable.empty()}
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.computation())
                .subscribe(randomGifState::onNext)
    }

    fun setRandomGeneratorActive(active: Boolean) {
        this.active = active
    }
}
