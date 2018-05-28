package com.kerman.isthebestfor.monese.models

import com.google.gson.annotations.SerializedName

data class Wind(
        @SerializedName("speed") var speed: Double? = null,
        @SerializedName("deg") var deg: Double = 90.0
)