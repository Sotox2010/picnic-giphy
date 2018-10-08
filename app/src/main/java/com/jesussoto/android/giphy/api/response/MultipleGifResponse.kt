package com.jesussoto.android.giphy.api.response

import com.google.gson.annotations.SerializedName
import com.jesussoto.android.giphy.model.GifObject

class MultipleGifResponse(

    @field:SerializedName("data")
    val data: List<GifObject>
)
