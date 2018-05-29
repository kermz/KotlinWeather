package com.kerman.isthebestfor.monese.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import java.util.*

data class WeatherModel(
        //@SerializedName("coord") var coord: WeatherCoordinates,
        @SerializedName("weather") var weather: List<Weather> = listOf(),
        @SerializedName("main") var main: Main = Main(),
        @SerializedName("wind") var wind: Wind = Wind(),
        var date: Date = Date(),
        var iconBitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
)
