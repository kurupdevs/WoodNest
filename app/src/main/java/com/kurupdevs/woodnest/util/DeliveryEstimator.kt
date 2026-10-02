package com.kurupdevs.woodnest.util

import com.kurupdevs.woodnest.util.toDateString

sealed interface DeliveryEstimate {
    data class Serviceable(
        val promisedAt: Long,
        val label: String,
        val isMetro: Boolean
    ) : DeliveryEstimate

    data object Unserviceable : DeliveryEstimate
}

object DeliveryEstimator {
    private val METRO = setOf("11", "40", "56", "50", "60", "70", "41", "38")

    fun estimate(pincode: String, now: Long = System.currentTimeMillis()): DeliveryEstimate {
        val p = pincode.trim()
        if (!p.matches(Regex("^[1-8][0-9]{5}$"))) return DeliveryEstimate.Unserviceable
        val metro = METRO.contains(p.substring(0, 2))
        val days = if (metro) 3 else 6
        val promised = now + days * 86400000L
        val label = if (metro) {
            "Delivery by ${promised.toDateString()} · 3 days"
        } else {
            "Delivery by ${promised.toDateString()} · 5–7 days"
        }
        return DeliveryEstimate.Serviceable(promised, label, metro)
    }
}
