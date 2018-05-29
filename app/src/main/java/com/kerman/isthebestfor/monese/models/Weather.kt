package com.kerman.isthebestfor.monese.models

import com.google.gson.annotations.SerializedName

data class Weather(
        @SerializedName("description") var description: String = "",
        @SerializedName("icon") var icon: String = ""
)
