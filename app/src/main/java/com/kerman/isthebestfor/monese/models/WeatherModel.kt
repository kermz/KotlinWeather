package com.kerman.isthebestfor.monese.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.kerman.isthebestfor.monese.models.Main
import com.kerman.isthebestfor.monese.models.Weather
import com.kerman.isthebestfor.monese.models.Wind
import java.util.*

data class WeatherModel (
    //@SerializedName("coord") var coord: WeatherCoordinates,
        @SerializedName("weather") var weather: List<Weather>? = null,
        @SerializedName("main") var main: Main? = null,
        @SerializedName("wind") var wind: Wind? = null,
        var date: Date? = null,
        var iconBitmap: Bitmap? = null

)
