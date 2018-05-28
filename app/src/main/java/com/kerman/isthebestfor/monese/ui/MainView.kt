package com.kerman.isthebestfor.monese.ui

import com.kerman.isthebestfor.monese.enums.ErrorTypes
import com.kerman.isthebestfor.monese.models.WeatherModel

interface MainView {
    fun showSpinner()
    fun hideSpinner()
    fun updateWeather(weather: List<WeatherModel>)
    fun showErrorToast(errorType: ErrorTypes)
}