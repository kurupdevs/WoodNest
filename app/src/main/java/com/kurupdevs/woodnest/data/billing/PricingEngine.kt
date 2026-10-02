package com.kurupdevs.woodnest.data.billing

data class BundleDef(
    val id: String,
    val name: String,
    val items: Map<String, Int>,
    val discountPct: Int,
    val blurb: String
)

data class BillLineInput(
    val productId: String,
    val name: String,
    val price: Int,
    val qty: Int,
    val isRental: Boolean,
    val monthly: Int = 0
)

data class Bill(
    val purchaseSubtotal: Int,
    val rentalSubtotal: Int,
    val rentalMonthlyTotal: Int,
    val bundleDiscount: Int,
    val promoCode: String?,
    val promoDiscount: Int,
    val promoError: String?,
    val exchangeCredit: Int,
    val loyaltyPointsUsed: Int,
    val loyaltyDiscount: Int,
    val deliveryFee: Int,
    val assemblyFee: Int,
    val consultFee: Int,
    val grandTotal: Int,
    val pointsToEarn: Int
)

object PricingEngine {
    val BUNDLES = mapOf(
        "living-room-glow" to BundleDef(
            "living-room-glow",
            "Living Room Glow-Up",
            mapOf("oslo-sofa" to 1, "nordic-coffee-table" to 1, "arc-floor-lamp" to 1),
            10,
            "Sofa + coffee table + arc lamp"
        ),
        "bedroom-retreat" to BundleDef(
            "bedroom-retreat",
            "Bedroom Retreat",
            mapOf("sheesham-bed" to 1, "drift-nightstand" to 2),
            12,
            "Bed + 2 nightstands"
        ),
        "work-from-home" to BundleDef(
            "work-from-home",
            "Work From Home",
            mapOf("focus-desk" to 1, "atlas-bookshelf" to 1, "ember-accent-chair" to 1),
            10,
            "Desk + bookshelf + chair"
        )
    )

    fun monthlyFor(price: Int): Int = kotlin.math.round(price * 0.085f).toInt()

    fun tierOf(points: Int): Int = when {
        points >= 5000 -> 2
        points >= 1000 -> 1
        else -> 0
    }

    fun computeBill(
        lines: List<BillLineInput>,
        promoCode: String? = null,
        useLoyalty: Boolean = false,
        loyaltyPoints: Int = 0,
        exchangeCredit: Int = 0,
        wantAssembly: Boolean = false,
        wantConsult: Boolean = false,
        referralPromoAvailable: Boolean = false
    ): Bill {
        val purchase = lines.filter { !it.isRental }
        val rental = lines.filter { it.isRental }
        val purchaseSubtotal = purchase.sumOf { it.price * it.qty }
        val rentalSubtotal = rental.sumOf { it.price * it.qty }
        val rentalMonthlyTotal = rental.sumOf { it.monthly * it.qty }

        var bundleDiscount = 0
        val qtyById = purchase.groupBy { it.productId }.mapValues { e -> e.value.sumOf { it.qty } }
        for ((_, b) in BUNDLES) {
            if (b.items.all { (pid, q) -> (qtyById[pid] ?: 0) >= q }) {
                val base = b.items.entries.sumOf { (pid, q) -> (purchase.find { it.productId == pid }?.price ?: 0) * q }
                bundleDiscount += (base * b.discountPct) / 100
            }
        }

        val afterBundle = purchaseSubtotal - bundleDiscount

        var promoDiscount = 0
        var promoError: String? = null
        val code = promoCode?.trim()?.uppercase()
        if (!code.isNullOrEmpty()) {
            when (code) {
                "WELCOME10" -> promoDiscount = minOf(afterBundle * 10 / 100, 2000)
                "FLAT500" -> if (afterBundle > 9999) promoDiscount = 500 else promoError = "Needs order above ₹9,999"
                "FREESHIP" -> {}
                "FESTIVE15" -> if (afterBundle > 24999) promoDiscount = afterBundle * 15 / 100 else promoError = "Needs order above ₹24,999"
                "REFBONUS500" -> if (referralPromoAvailable && afterBundle > 4999) promoDiscount = 500 else promoError = "Referral coupon not available"
                else -> promoError = "Invalid code"
            }
        }

        val ex = exchangeCredit.coerceIn(0, afterBundle)
        val afterEx = afterBundle - promoDiscount - ex

        val tier = tierOf(loyaltyPoints)
        var loyaltyPointsUsed = 0
        var loyaltyDiscount = 0
        if (useLoyalty && loyaltyPoints >= 100) {
            val maxUse = minOf(loyaltyPoints / 100 * 100, afterEx)
            loyaltyPointsUsed = maxUse
            loyaltyDiscount = maxUse
        }

        val afterLoyalty = afterEx - loyaltyDiscount
        val deliveryFee = if (code == "FREESHIP" || afterLoyalty >= 15000 || afterLoyalty == 0) 0 else 499
        val assemblyFee = if (!wantAssembly) 0 else if (tier == 2 || afterLoyalty >= 20000) 0 else 499
        val consultFee = if (!wantConsult) 0 else if (afterLoyalty >= 25000) 0 else 499
        val grandTotal = (afterLoyalty + deliveryFee + assemblyFee + consultFee).coerceAtLeast(0)

        val mult = when (tier) {
            2 -> 1.5f
            1 -> 1.25f
            else -> 1f
        }
        val pointsToEarn = (grandTotal / 100 * mult).toInt()

        return Bill(
            purchaseSubtotal, rentalSubtotal, rentalMonthlyTotal, bundleDiscount,
            code, promoDiscount, promoError, ex, loyaltyPointsUsed, loyaltyDiscount,
            deliveryFee, assemblyFee, consultFee, grandTotal, pointsToEarn
        )
    }
}
