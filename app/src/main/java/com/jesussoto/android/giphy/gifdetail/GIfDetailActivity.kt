package com.jesussoto.android.giphy.gifdetail

import android.animation.ObjectAnimator
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.jesussoto.android.giphy.R
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.di.GlideApp
import com.jesussoto.android.giphy.model.GifObject
import com.jesussoto.android.giphy.repository.GifRepository
import com.jesussoto.android.giphy.widget.AlwaysEnterToolbarScrollListener
import com.jesussoto.android.giphy.widget.SynchronizedScrollView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_gif_detail.*

class GIfDetailActivity : AppCompatActivity(), SynchronizedScrollView.OnScrollListener {

    @ColorInt
    private var toolbarColor = Color.TRANSPARENT

    private lateinit var toolbarColorAnimator: ObjectAnimator

    private var toolbarColored = false

    private lateinit var gif: GifObject

    private lateinit var viewModel: GifDetailViewModel

    private var disposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!intent.hasExtra(EXTRA_GIF_OBJECT)) {
            Toast.makeText(this, "No gif provided, exiting...", Toast.LENGTH_SHORT).show();
            finish()
            return
        }

        setContentView(R.layout.activity_gif_detail)
        gif = intent.getParcelableExtra(EXTRA_GIF_OBJECT)

        setupToolbar()
        scrollView.addOnScrollListener(AlwaysEnterToolbarScrollListener(toolbar));
        scrollView.addOnScrollListener(this)

        bindGif(gif)
        viewModel = getViewModel()
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // Properly restore the toolbar scroll position.
        scrollView.post {
            scrollView.onScrollListeners?.forEach { listener ->
                listener.onScrollChanged(0, 0, scrollView.scrollX, scrollView.scrollY)
            }
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

    private fun bindViewModel() {
        disposables = CompositeDisposable()
        disposables?.add(viewModel.randomGifState
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onRandomGifStatusChange))
        viewModel.setRandomGeneratorActive(true)
    }

    private fun unbindViewModel() {
        viewModel.setRandomGeneratorActive(false)
        disposables?.dispose()
        disposables = null
    }

    private fun getViewModel(): GifDetailViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = GifRepository.instance
                @Suppress("UNCHECKED_CAST")
                return GifDetailViewModel(repo) as T
            }
        })[GifDetailViewModel::class.java]
    }

    /**
     * Update the ui when a new random gif is ready.
     */
    private fun onRandomGifStatusChange(resource: com.jesussoto.android.giphy.api.Resource<GifObject>) {
        Log.d("GifDetail", resource.toString())
        when (resource.status) {
            Resource.Status.LOADING -> {
                randomErrorIcon.visibility = View.GONE
                randomProgress.visibility = View.VISIBLE
                randomGifTitle.text = getString(R.string.random_loading)
            }
            Resource.Status.SUCCESS -> {
                randomErrorIcon.visibility = View.GONE
                randomProgress.visibility = View.GONE
                randomGifTitle.text = resource.data?.title
                loadRandomGif(resource.data)
            }
            Resource.Status.ERROR -> {
                randomErrorIcon.visibility = View.VISIBLE
                randomProgress.visibility = View.GONE
                randomGifTitle.text = getString(R.string.random_error)
            }
        }
    }

    private fun loadRandomGif(randomGif: GifObject?) {
        if (randomGif == null) {
            return
        }

        val fixedWidth = randomGif.images.fixedWidth

        GlideApp.with(this)
                .asGif()
                .load(fixedWidth.url)
                .placeholder(R.drawable.image_placeholder)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .into(randomGifView)
    }

    /**
     * Setup toolbar properties.
     */
    private fun setupToolbar() {
        if (toolbarColor == Color.TRANSPARENT) {
            toolbarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            toolbar.post { toolbar.title = null }
        }

        // Init the color animator for the toolbar.
        toolbarColorAnimator = ObjectAnimator.ofArgb(toolbar, "backgroundColor", 0)
        toolbarColorAnimator.interpolator = LinearInterpolator()
        toolbarColorAnimator.setAutoCancel(true)
    }

    /**
     * Update view to represent the underlying gif details.
     *
     * @param gif the [GifObject] to bind to.
     */
    private fun bindGif(gif: GifObject) {
        val original = gif.images.original
        val width = original.width.toFloat()
        val height = original.height.toFloat()

        // Make the gif view of the same aspect ratio than the original version.
        gifView.aspectRatio = width / height

        GlideApp.with(this)
                .asGif()
                .load(original.url)
                .placeholder(R.drawable.image_placeholder)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                .listener(gifLoadListener)
                .into(gifView)

        val titleFallback = getString(R.string.no_title)
        gifTitleView.text = if (!TextUtils.isEmpty(gif.title)) gif.title else titleFallback
    }

    /**
     * Callback invoked when the poster bitmap palette has been processed.
     * Here we tweak the UI based on the palette for a more immersive experience.
     *
     * @param palette The processed {@link Palette}
     */
    private fun onPaletteGenerated(palette: Palette?) {
        if (palette == null) {
            return
        }

        var mostPopulousSwatch: Palette.Swatch? = null
        for (swatch in palette.swatches) {
            if (mostPopulousSwatch == null || swatch.population > mostPopulousSwatch.population) {
                mostPopulousSwatch = swatch
            }
        }

        if (mostPopulousSwatch != null) {
            window.statusBarColor = mostPopulousSwatch.rgb
        }
    }

    /**
     * Listen for scroll events to animate the toolbar.
     */
    override fun onScrollChanged(left: Int, top: Int, deltaX: Int, deltaY: Int) {
        val computedHeight = gifView.height
                - toolbar.height
                - toolbar.translationY;

        val shouldColorize = top >= computedHeight
        animateToolbar(shouldColorize)
    }

    /**
     * Animates toolbar show or hide.
     *
     * @param show whether to show the toolbar or hide it.
     */
    private fun animateToolbar(show: Boolean) {
        if (show != toolbarColored) {
            val startColor = if (show) Color.TRANSPARENT else toolbarColor
            val endColor = if (show) toolbarColor else Color.TRANSPARENT
            val duration = if (show) 0L else ANIMATION_DURATION

            toolbarColorAnimator.setIntValues(startColor, endColor)
            toolbarColorAnimator.duration = duration
            toolbarColorAnimator.start()
            toolbarColored = show
        }
    }

    // Custom Glide listener to be able to receive the gif bitmap for further color processing
    // using the Palette API.
    private val gifLoadListener = object : RequestListener<GifDrawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            if (resource != null) {
                val bitmap = resource.firstFrame ?: return false
                val regionHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        REGION_HEIGHT_DP, this@GIfDetailActivity.resources.displayMetrics).toInt()

                Palette.from(bitmap)
                        .maximumColorCount(3)
                        .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                        .setRegion(0, 0, bitmap.width - 1, regionHeight)
                        .generate(this@GIfDetailActivity::onPaletteGenerated)
            }
            return false
        }
    }

    companion object {
        private const val EXTRA_GIF_OBJECT = "extra_gif_object"

        private const val ANIMATION_DURATION = 200L

        /**
         * Region to limit the gif color palette processing to the top.
         */
        private const val REGION_HEIGHT_DP = 24f

        @JvmStatic
        fun start(launching: FragmentActivity, gif: GifObject) {
            val intent = Intent(launching, GIfDetailActivity::class.java)
            intent.putExtra(EXTRA_GIF_OBJECT, gif)
            launching.startActivity(intent)
        }
    }


}
