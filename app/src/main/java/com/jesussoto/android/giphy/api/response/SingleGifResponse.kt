package com.jesussoto.android.giphy.api.response

import com.google.gson.annotations.SerializedName
import com.jesussoto.android.giphy.model.GifObject

class SingleGifResponse(

    @field:SerializedName("data")
    val data: GifObject
)
