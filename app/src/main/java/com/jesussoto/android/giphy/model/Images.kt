package com.jesussoto.android.giphy.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Images(

    @field:SerializedName("fixed_width")
    val fixedWidth: ImageMeta,

    @field:SerializedName("fixed_width_downsampled")
    val fixedWidthDownsampled: ImageMeta,

    @field:SerializedName("original")
    val original: ImageMeta

) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(ImageMeta::class.java.classLoader),
            parcel.readParcelable(ImageMeta::class.java.classLoader),
            parcel.readParcelable(ImageMeta::class.java.classLoader))

    data class ImageMeta(

        @field:SerializedName("url")
        val url: String,

        @field:SerializedName("width")
        val width: String,

        @field:SerializedName("height")
        val height: String,

        @field:SerializedName("size")
        val size: String

    ) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(url)
            parcel.writeString(width)
            parcel.writeString(height)
            parcel.writeString(size)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ImageMeta> {
            override fun createFromParcel(parcel: Parcel): ImageMeta {
                return ImageMeta(parcel)
            }

            override fun newArray(size: Int): Array<ImageMeta?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(fixedWidth, flags)
        parcel.writeParcelable(fixedWidthDownsampled, flags)
        parcel.writeParcelable(original, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Images> {
        override fun createFromParcel(parcel: Parcel): Images {
            return Images(parcel)
        }

        override fun newArray(size: Int): Array<Images?> {
            return arrayOfNulls(size)
        }
    }
}
