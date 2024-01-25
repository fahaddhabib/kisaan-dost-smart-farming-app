package com.project.farmingapp.model

import java.io.Serializable

class WeatherModel : Serializable {
    var timeNow: String? = null
    var descWeather: String? = null
    var currentTemp = 0.0
    var tempMax = 0.0
    var tempMin = 0.0
}