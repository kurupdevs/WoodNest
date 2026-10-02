package com.kurupdevs.woodnest.ui.nav

object Routes {
    const val HOME = "home"
    const val REELS = "reels"
    const val FAVORITES = "favorites"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val PROFILE = "profile"
    const val PRODUCT = "product/{productId}"
    const val CHECKOUT = "checkout"
    const val ORDER_DETAIL = "orderDetail/{orderId}"
    const val OFFERS = "offers"
    const val ADDRESSES = "addresses"
    const val CONSULTS = "consults"
    const val NOTIFICATIONS = "notifications"
    const val REFERRAL = "referral"
    const val LOYALTY = "loyalty"

    fun productRoute(id: String) = "product/$id"
    fun orderRoute(id: String) = "orderDetail/$id"
}
