package com.limereport.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One shift, logged at the end of the shift. Rates are copied in when the shift is
 * created, so changing the rates in Settings later does not rewrite past earnings.
 */
@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startMillis: Long,
    val endMillis: Long,
    val relocateScooters: Int = 0,
    val relocateBikes: Int = 0,
    val deployScooters: Int = 0,
    val deployBikes: Int = 0,
    val damagedVehicles: Int = 0,
    val relocateScooterRate: Int,
    val relocateBikeRate: Int,
    val deployScooterRate: Int,
    val deployBikeRate: Int,
    val damagedRate: Int,
    val note: String = "",
)

/** Earnings for the shift in shekels, using the rates saved with it. */
val Shift.total: Int
    get() = relocateScooters * relocateScooterRate +
        relocateBikes * relocateBikeRate +
        deployScooters * deployScooterRate +
        deployBikes * deployBikeRate +
        damagedVehicles * damagedRate
