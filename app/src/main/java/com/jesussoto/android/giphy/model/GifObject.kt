package com.jesussoto.android.giphy.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class GifObject(

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("slug")
    val slug: String,

    @field:SerializedName("url")
    val url: String,

    @field:SerializedName("bitly_url")
    val bitlyUrl: String,

    @field:SerializedName("embed_url")
    val embedUrl: String,

    @field:SerializedName("username")
    val username: String,

    @field:SerializedName("source")
    val source: String,

    @field:SerializedName("rating")
    val rating: String,

    @field:SerializedName("content_url")
    val contentUrl: String,

    @field:SerializedName("source_tld")
    val sourceTld: String,

    @field:SerializedName("source_post_url")
    val sourcePostUrl: String,

    @field:SerializedName("update_datetime")
    val updateDatetime: String?, // "2013-08-01 12:41:48"

    @field:SerializedName("create_datetime")
    val createDatetime: String?,

    @field:SerializedName("import_datetime")
    val importDatetime: String?,

    @field:SerializedName("trending_datetime")
    val trendingDatetime: String?,

    @field:SerializedName("title")
    val title: String?,

    @field:SerializedName("images")
    val images: Images

) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readParcelable(Images.ImageMeta::class.java.classLoader))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(id)
        parcel.writeString(slug)
        parcel.writeString(url)
        parcel.writeString(bitlyUrl)
        parcel.writeString(embedUrl)
        parcel.writeString(username)
        parcel.writeString(source)
        parcel.writeString(rating)
        parcel.writeString(contentUrl)
        parcel.writeString(sourceTld)
        parcel.writeString(sourcePostUrl)
        parcel.writeString(updateDatetime)
        parcel.writeString(createDatetime)
        parcel.writeString(importDatetime)
        parcel.writeString(trendingDatetime)
        parcel.writeString(title)
        parcel.writeParcelable(images, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GifObject> {
        override fun createFromParcel(parcel: Parcel): GifObject {
            return GifObject(parcel)
        }

        override fun newArray(size: Int): Array<GifObject?> {
            return arrayOfNulls(size)
        }
    }

}