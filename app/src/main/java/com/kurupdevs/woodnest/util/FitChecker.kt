package com.kurupdevs.woodnest.util

enum class FitVerdict(val title: String, val desc: String) {
    FITS_EASILY(
        "Fits easily",
        "Your door and staircase clear the packaged size comfortably."
    ),
    TIGHT(
        "Tight — measure twice",
        "It may fit, but clear the path and measure again."
    ),
    WONT_FIT(
        "Won't fit",
        "The packaged size exceeds your narrowest opening."
    )
}

object FitChecker {
    fun check(doorW: Int, stairW: Int, packW: Int, packH: Int, packD: Int): FitVerdict {
        val c = minOf(doorW, stairW) - maxOf(packW, packH, packD)
        return when {
            c >= 5 -> FitVerdict.FITS_EASILY
            c >= 0 -> FitVerdict.TIGHT
            else -> FitVerdict.WONT_FIT
        }
    }
}
