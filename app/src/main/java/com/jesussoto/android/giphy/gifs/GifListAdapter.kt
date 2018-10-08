package com.jesussoto.android.giphy.gifs

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.jesussoto.android.giphy.R
import com.jesussoto.android.giphy.api.Resource
import com.jesussoto.android.giphy.api.Resource.Status.ERROR
import com.jesussoto.android.giphy.api.Resource.Status.LOADING
import com.jesussoto.android.giphy.di.GlideApp
import com.jesussoto.android.giphy.model.GifObject
import com.jesussoto.android.giphy.widget.AspectRatioImageView

class GifListAdapter constructor(
        private val retryCallback: () -> Unit) :
        PagedListAdapter<GifObject, RecyclerView.ViewHolder>(GIF_COMPARATOR) {

    private var networkState: Resource.Status? = null

    private var onGifTappedListener: ((GifObject) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_PROGRESS) {
            return NetworkStateItemViewHolder.create(parent, retryCallback)
        }

        val holder = GifViewHolder.create(parent)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onGifTappedListener?.invoke(getItem(position)!!)
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GifViewHolder) {
            holder.bindGif(getItem(position))
        } else {
            (holder as NetworkStateItemViewHolder).bindState(networkState)
            val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1)
            VIEW_TYPE_PROGRESS
        else
            VIEW_TYPE_ITEM
    }

    internal fun hasExtraRow(): Boolean {
        return networkState != null && networkState != Resource.Status.SUCCESS
    }

    internal fun setNetworkState(state: Resource.Status) {
        val previousState = networkState
        val previousExtraRow = hasExtraRow()
        networkState = state
        val newExtraRow = hasExtraRow()
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (newExtraRow && previousState != state) {
            notifyItemChanged(itemCount - 1)
        }
    }

    internal fun setOnGifTappedListener(listener: ((GifObject) -> Unit)?) {
        onGifTappedListener = listener
    }

    internal class GifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val gifView = itemView.findViewById<AspectRatioImageView>(R.id.gif_view)
        private val context = itemView.context

        fun bindGif(gif: GifObject?) {
            if (gif == null) {
                return
            }

            val fixedWidthImage = gif.images.fixedWidth
            val width = fixedWidthImage.width.toFloat()
            val height = fixedWidthImage.height.toFloat()
            gifView.aspectRatio = width / height

            GlideApp.with(context)
                    .asGif()
                    .load(fixedWidthImage.url)
                    .placeholder(R.drawable.image_placeholder)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .apply(RequestOptions.priorityOf(Priority.IMMEDIATE))
                    .into(gifView)
        }

        companion object {

            fun create(parent: ViewGroup): GifViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_gif, parent, false)

                return GifViewHolder(itemView)
            }
        }
    }

    internal class NetworkStateItemViewHolder(itemView: View, retryCallback: () -> Unit) :
            RecyclerView.ViewHolder(itemView) {

        private val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)
        private val errorMessage = itemView.findViewById<TextView>(R.id.error_msg)
        private val retryButton = itemView.findViewById<Button>(R.id.retry_button)

        init {
            retryButton.setOnClickListener { retryCallback.invoke() }
        }

        fun bindState(networkState: Resource.Status?) {
            progressBar.visibility = if (networkState == LOADING) View.VISIBLE else View.GONE
            errorMessage.visibility = if (networkState == ERROR) View.VISIBLE else View.GONE
            retryButton.visibility = if (networkState == ERROR) View.VISIBLE else View.GONE
        }

        companion object {

            @JvmStatic
            fun create(parent: ViewGroup, retryCallback: () -> Unit): NetworkStateItemViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(
                        R.layout.list_item_network_state, parent, false)
                return NetworkStateItemViewHolder(itemView, retryCallback)
            }
        }
    }

    companion object {

        /**
         * There are two layout types we define in this adapter:
         * 1. Progress footer view
         * 2. Gif item view
         */
        private const val VIEW_TYPE_PROGRESS = 0
        private const val VIEW_TYPE_ITEM = 1

        val GIF_COMPARATOR = object : DiffUtil.ItemCallback<GifObject>() {
            override fun areContentsTheSame(oldItem: GifObject, newItem: GifObject): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: GifObject, newItem: GifObject): Boolean =
                    TextUtils.equals(oldItem.id, newItem.id)
        }
    }
}
