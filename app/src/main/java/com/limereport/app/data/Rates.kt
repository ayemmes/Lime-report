package com.limereport.app.data

import android.content.Context

/** Price in shekels per vehicle for each task type. */
data class Rates(
    val relocateScooter: Int = 10,
    val relocateBike: Int = 14,
    val deployScooter: Int = 8,
    val deployBike: Int = 12,
    val damaged: Int = 14,
    val batterySwap: Int = 10,
)

class RatesStore(context: Context) {
    private val prefs = context.getSharedPreferences("rates", Context.MODE_PRIVATE)

    fun load(): Rates {
        val d = Rates()
        return Rates(
            relocateScooter = prefs.getInt("relocateScooter", d.relocateScooter),
            relocateBike = prefs.getInt("relocateBike", d.relocateBike),
            deployScooter = prefs.getInt("deployScooter", d.deployScooter),
            deployBike = prefs.getInt("deployBike", d.deployBike),
            damaged = prefs.getInt("damaged", d.damaged),
            batterySwap = prefs.getInt("batterySwap", d.batterySwap),
        )
    }

    fun save(rates: Rates) {
        prefs.edit()
            .putInt("relocateScooter", rates.relocateScooter)
            .putInt("relocateBike", rates.relocateBike)
            .putInt("deployScooter", rates.deployScooter)
            .putInt("deployBike", rates.deployBike)
            .putInt("damaged", rates.damaged)
            .putInt("batterySwap", rates.batterySwap)
            .apply()
    }
}
