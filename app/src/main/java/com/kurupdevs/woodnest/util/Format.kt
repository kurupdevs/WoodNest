package com.kurupdevs.woodnest.util

fun Int.inr(): String =
    "₹" + java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN")).format(this)

fun Long.toDateString(): String =
    java.time.Instant.ofEpochMilli(this)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()
        .format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"))
