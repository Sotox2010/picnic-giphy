package com.jesussoto.android.giphy.gifs

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

import com.jesussoto.android.giphy.R
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.model.GifObject
import com.jesussoto.android.giphy.gifdetail.GIfDetailActivity
import com.jesussoto.android.giphy.repository.GifRepository
import com.jesussoto.android.giphy.widget.AlwaysEnterToolbarScrollListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gif_stream.*

class GifStreamActivity : AppCompatActivity() {

    private lateinit var adapter: GifListAdapter

    private lateinit var alwaysEnterToolbarScrollListener: AlwaysEnterToolbarScrollListener

    private lateinit var viewModel: GifStreamViewModel

    private var disposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gif_stream)

        setupToolbar()
        setupRecyclerView()

        emptyAction.setOnClickListener { viewModel.retryLastFetch() }

        viewModel = getViewModel()
        viewModel.pagedGifList.observe(this, Observer(adapter::submitList))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the toolbar animation state by manually calling the scroll listener.
        gifRecyclerView.post {
            alwaysEnterToolbarScrollListener.onScrolled(gifRecyclerView,
                    gifRecyclerView.computeHorizontalScrollOffset(),
                    gifRecyclerView.computeVerticalScrollOffset())
        }
    }

    override fun onStart() {
        super.onStart()
        bindViewModel()
    }

    override fun onStop() {
        super.onStop()
        unbindViewModel()
    }

    private fun getViewModel(): GifStreamViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = GifRepository.instance
                @Suppress("UNCHECKED_CAST")
                return GifStreamViewModel(repo) as T
            }
        })[GifStreamViewModel::class.java]
    }

    /**
     * Bind to the view model to react to data changes.
     */
    private fun bindViewModel() {
        disposables = CompositeDisposable()

        disposables?.add(viewModel.networkState
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::setNetworkState))

        disposables?.add(viewModel.initialLoadState
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView))
    }

    private fun unbindViewModel() {
        disposables?.dispose()
        disposables = null
    }

    /**
     * Set up the toolbar and the filter spinner as well.
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        // Remove the default title set by the system in favor of the filter spinner.
        toolbar.post {
            toolbar.title = null
            toolbar.setPadding(toolbar.paddingLeft, toolbar.paddingTop, toolbar.paddingRight, 0)
        }

        // Adjust the recycler view top padding according to the current toolbar height.
        toolbar.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val extra = resources.getDimensionPixelOffset(R.dimen.gif_grid_margin_vertical)
            gifRecyclerView.setPadding(gifRecyclerView.paddingLeft,
                    bottom - top + extra, gifRecyclerView.paddingRight, gifRecyclerView.bottom)
        }
    }

    /**
     * Set up the gif grid layout and add the toolbar animation scroll listener.
     */
    private fun setupRecyclerView() {
        val spanCount = resources.getInteger(R.integer.gif_grid_span_count)
        alwaysEnterToolbarScrollListener = AlwaysEnterToolbarScrollListener(toolbar)

        adapter = GifListAdapter { viewModel.retryLastFetch() }
        adapter.setOnGifTappedListener(this::navigateToGifDetail)

        // This grid layout manager with custom span size lookup will ensure that the 'loading' view
        // takes the whole row size instead of a single cell size.
        val layoutManager = StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL)

        gifRecyclerView.layoutManager = layoutManager
        gifRecyclerView.adapter = adapter
        gifRecyclerView.addOnScrollListener(alwaysEnterToolbarScrollListener)
    }

    /**
     * Updates the view based on the UiModel state, this gets called each time new data is
     * available to display in the UI.
     *
     * @param initialLoadState with all the data to display in the UI.
     */
    private fun updateView(initialLoadState: Resource.Status) {
        val listVisibility = if (initialLoadState === Resource.Status.SUCCESS) View.VISIBLE else View.GONE
        val progressVisibility = if (initialLoadState === Resource.Status.LOADING) View.VISIBLE else View.GONE
        val errorVisibility = if (initialLoadState === Resource.Status.ERROR) View.VISIBLE else View.GONE

        gifRecyclerView.visibility = listVisibility
        progressIndicator.visibility = progressVisibility
        emptyContainer.visibility = errorVisibility
    }

    /**
     * Navigate to [GIfDetailActivity] to show the details of the given [GifObject].
     *
     * @param gif the [GifObject] to show its details.
     */
    private fun navigateToGifDetail(gif: GifObject) {
        GIfDetailActivity.start(this, gif)
    }

}
