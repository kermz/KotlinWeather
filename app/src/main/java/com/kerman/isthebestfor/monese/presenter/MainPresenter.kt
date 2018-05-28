package com.kerman.isthebestfor.monese.presenter

import com.kerman.isthebestfor.monese.api.OpenWeatherAPI
import com.kerman.isthebestfor.monese.enums.ErrorTypes
import com.kerman.isthebestfor.monese.models.WeatherModel
import com.kerman.isthebestfor.monese.ui.MainView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class MainPresenter(val view : MainView) {
    @Inject
    lateinit var api : OpenWeatherAPI

    fun getWeather(lat : String, lon : String) {
        view.showSpinner()
        api.currentWeather(lat, lon).enqueue(object : Callback<WeatherModel> {

            override fun onResponse(call: Call<WeatherModel>, response: Response<WeatherModel>) {
                response.body()?.let {
                    createListForView(it)
                    view.hideSpinner()
                } ?: view.showErrorToast(ErrorTypes.NO_RESULT_FOUND)
            }

            override fun onFailure(call: Call<WeatherModel>?, t: Throwable) {
                view.showErrorToast(ErrorTypes.CALL_ERROR)
                t.printStackTrace()
            }
        })
    }
    private fun  createListForView(weatherResponse : WeatherModel) {
        val weather = mutableListOf<WeatherModel>()
        val rightNow = Calendar.getInstance()
        val weatherItem = WeatherModel(weatherResponse.weather, weatherResponse.main, weatherResponse.wind, rightNow.time)
        weather.add(weatherItem)

        view.updateWeather(weather)
    }

}