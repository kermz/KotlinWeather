package com.kerman.isthebestfor.monese.api


import com.kerman.isthebestfor.monese.models.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherAPI {

    @GET("weather?")
    fun currentWeather(@Query("lat") lat: String, @Query("lon") lon: String): Call<WeatherModel>

    companion object {
        const val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    }
}