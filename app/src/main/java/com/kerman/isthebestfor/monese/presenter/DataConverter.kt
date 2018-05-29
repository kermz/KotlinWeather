package com.kerman.isthebestfor.monese.presenter

class DataConverter {

    fun getDirection(x: Double?, arrayList: List<String>): String {
        return arrayList[kotlin.math.round(((x!! % 360) / 45) % 8).toInt()]
    }

}
