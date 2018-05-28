package com.kerman.isthebestfor.monese.api

import com.kerman.isthebestfor.monese.presenter.MainPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(OpenWeatherAPIModule::class)])
interface OpenWeatherAPIComponent {
    fun inject(presenter: MainPresenter);
}