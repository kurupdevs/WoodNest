package com.kurupdevs.woodnest.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val mrp: Int,
    val description: String,
    val material: String,
    val imageName: String,
    val rating: Float,
    val reviewCount: Int,
    val widthCm: Int,
    val heightCm: Int,
    val depthCm: Int,
    val packW: Int,
    val packH: Int,
    val packD: Int,
    val stock: Int,
    val region: String?
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val qty: Int,
    val isRental: Boolean,
    val addedAt: Long
)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val productId: String,
    val priceAtAdd: Int,
    val alertsOn: Boolean,
    val addedAt: Long
)

@Entity(tableName = "recent_views")
data class RecentView(
    @PrimaryKey val productId: String,
    val viewedAt: Long
)

@Entity(tableName = "addresses")
data class Address(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val line1: String,
    val line2: String,
    val city: String,
    val state: String,
    val pincode: String,
    val isDefault: Boolean
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String,
    val placedAt: Long,
    val status: String,
    val subtotal: Int,
    val promoCode: String?,
    val promoDiscount: Int,
    val bundleDiscount: Int,
    val exchangeCredit: Int,
    val exchangeDesc: String?,
    val loyaltyPointsUsed: Int,
    val loyaltyDiscount: Int,
    val deliveryFee: Int,
    val assemblyFee: Int,
    val assemblyBooked: Boolean,
    val assemblySlot: String?,
    val consultFee: Int,
    val grandTotal: Int,
    val pointsEarned: Int,
    val promisedAt: Long,
    val deliverySlot: String,
    val addressId: Long,
    val paymentMode: String = "DEMO"
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val productId: String,
    val name: String,
    val price: Int,
    val qty: Int,
    val isRental: Boolean,
    val monthly: Int,
    val monthsPaid: Int = 0,
    val boughtOut: Boolean = false
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val userName: String,
    val stars: Int,
    val text: String,
    val photoUri: String?,
    val verified: Boolean,
    val createdAt: Long
)

@Entity(tableName = "consults")
data class Consult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val slot: String,
    val status: String,
    val fee: Int,
    val createdAt: Long
)

@Entity(tableName = "loyalty")
data class LoyaltyAccount(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val joined: Boolean = false,
    val referralCode: String = "",
    val referralPromoUsed: Boolean = false,
    val totalEarned: Int = 0
)

@Entity(tableName = "price_history")
data class PriceHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: String,
    val price: Int,
    val changedAt: Long
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val createdAt: Long,
    val read: Boolean = false
)

@Entity(tableName = "reel_likes")
data class ReelLike(
    @PrimaryKey val reelId: String
)
