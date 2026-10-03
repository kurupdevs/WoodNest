package com.kurupdevs.woodnest.data.repo

import android.content.Context
import com.kurupdevs.woodnest.data.billing.Bill
import com.kurupdevs.woodnest.data.billing.PricingEngine
import com.kurupdevs.woodnest.data.db.Address
import com.kurupdevs.woodnest.data.db.AppDatabase
import com.kurupdevs.woodnest.data.db.AppNotification
import com.kurupdevs.woodnest.data.db.CartItem
import com.kurupdevs.woodnest.data.db.Consult
import com.kurupdevs.woodnest.data.db.Favorite
import com.kurupdevs.woodnest.data.db.LoyaltyAccount
import com.kurupdevs.woodnest.data.db.Order
import com.kurupdevs.woodnest.data.db.OrderItem
import com.kurupdevs.woodnest.data.db.Product
import com.kurupdevs.woodnest.data.db.RecentView
import com.kurupdevs.woodnest.data.db.ReelLike
import com.kurupdevs.woodnest.data.db.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WoodNestRepository(private val db: AppDatabase, private val context: Context) {

    data class CartLine(val item: CartItem, val product: Product)
    data class PriceDrop(val product: Product, val oldPrice: Int, val newPrice: Int)
    data class UserProfile(val name: String, val phone: String)
    data class OrderInput(
        val addressId: Long,
        val slot: String,
        val promisedAt: Long,
        val promoCode: String?,
        val useLoyalty: Boolean,
        val loyaltyPointsUsed: Int,
        val exchangeCredit: Int,
        val exchangeDesc: String?,
        val assembly: Boolean,
        val assemblySlot: String?,
        val consult: Consult?,
        val bill: Bill
    )

    private val prefs = context.getSharedPreferences("woodnest", Context.MODE_PRIVATE)

    fun productsFlow() = db.productDao().all()
    fun productFlow(id: String) = db.productDao().byId(id)
    suspend fun productById(id: String) = db.productDao().byIdOnce(id)
    fun searchProducts(q: String) = db.productDao().search(q)
    fun productsByRegion(r: String) = db.productDao().byRegion(r)

    fun cartLines(): Flow<List<CartLine>> = db.cartDao().all().map { items ->
        items.mapNotNull { i -> db.productDao().byIdOnce(i.productId)?.let { CartLine(i, it) } }
    }

    suspend fun addToCart(productId: String, qty: Int = 1, isRental: Boolean = false) {
        val e = db.cartDao().find(productId, isRental)
        if (e == null) {
            db.cartDao().insert(
                CartItem(productId = productId, qty = qty, isRental = isRental, addedAt = System.currentTimeMillis())
            )
        } else {
            db.cartDao().update(e.copy(qty = e.qty + qty))
        }
    }

    suspend fun updateQty(id: Long, qty: Int) {
        if (qty <= 0) {
            db.cartDao().delete(id)
        } else {
            val cur = db.cartDao().all().first().find { it.id == id }
            if (cur != null) db.cartDao().update(cur.copy(qty = qty))
        }
    }

    suspend fun removeFromCart(id: Long) = db.cartDao().delete(id)
    suspend fun clearCart() = db.cartDao().clear()

    fun favoritesFlow(): Flow<List<Pair<Favorite, Product>>> =
        combine(db.favoriteDao().all(), db.productDao().all()) { favs, prods ->
            favs.mapNotNull { f -> prods.find { it.id == f.productId }?.let { f to it } }
        }

    fun favoriteIdsFlow(): Flow<Set<String>> = db.favoriteDao().ids().map { it.toSet() }

    suspend fun toggleFavorite(productId: String) {
        val e = db.favoriteDao().byId(productId)
        if (e == null) {
            val p = db.productDao().byIdOnce(productId)
            if (p != null) db.favoriteDao().insert(Favorite(productId, p.price, true, System.currentTimeMillis()))
        } else {
            db.favoriteDao().delete(productId)
        }
    }

    suspend fun setAlert(pid: String, on: Boolean) = db.favoriteDao().setAlerts(pid, on)

    fun dropCount(): Flow<Int> = favoritesFlow().map { list ->
        list.count { (f, p) -> f.alertsOn && p.price < f.priceAtAdd }
    }

    fun priceDrops(): Flow<List<PriceDrop>> = favoritesFlow().map { list ->
        list.filter { (f, p) -> f.alertsOn && p.price < f.priceAtAdd }
            .map { (f, p) -> PriceDrop(p, f.priceAtAdd, p.price) }
    }

    fun addressesFlow() = db.addressDao().all()
    fun defaultAddressFlow() = db.addressDao().defaultAddress()
    suspend fun defaultAddressOnce() = db.addressDao().defaultOnce()

    suspend fun upsertAddress(a: Address): Long =
        if (a.id == 0L) db.addressDao().insert(a) else {
            db.addressDao().update(a)
            a.id
        }

    suspend fun deleteAddress(id: Long) = db.addressDao().delete(id)

    suspend fun setDefaultAddress(id: Long) {
        db.addressDao().clearDefault()
        db.addressDao().setDefault(id)
    }

    suspend fun placeOrder(input: OrderInput): String {
        val lines = cartLines().first()
        val oid = "WN" + System.currentTimeMillis().toString().takeLast(8)
        val now = System.currentTimeMillis()
        val b = input.bill

        db.orderDao().insert(
            Order(
                id = oid, placedAt = now, status = "PLACED",
                subtotal = b.purchaseSubtotal + b.rentalSubtotal,
                promoCode = b.promoCode, promoDiscount = b.promoDiscount,
                bundleDiscount = b.bundleDiscount, exchangeCredit = b.exchangeCredit,
                exchangeDesc = input.exchangeDesc,
                loyaltyPointsUsed = b.loyaltyPointsUsed, loyaltyDiscount = b.loyaltyDiscount,
                deliveryFee = b.deliveryFee, assemblyFee = b.assemblyFee,
                assemblyBooked = input.assembly, assemblySlot = input.assemblySlot,
                consultFee = b.consultFee, grandTotal = b.grandTotal,
                pointsEarned = b.pointsToEarn, promisedAt = input.promisedAt,
                deliverySlot = input.slot, addressId = input.addressId
            )
        )

        db.orderItemDao().insertAll(
            lines.map {
                OrderItem(
                    orderId = oid, productId = it.product.id, name = it.product.name,
                    price = it.product.price, qty = it.item.qty, isRental = it.item.isRental,
                    monthly = PricingEngine.monthlyFor(it.product.price)
                )
            }
        )

        input.consult?.let { db.consultDao().insert(it.copy(status = "BOOKED", createdAt = now)) }

        val loy = db.loyaltyDao().getOnce() ?: LoyaltyAccount()
        db.loyaltyDao().save(
            loy.copy(
                points = (loy.points - b.loyaltyPointsUsed + b.pointsToEarn).coerceAtLeast(0),
                totalEarned = loy.totalEarned + b.pointsToEarn,
                referralPromoUsed = loy.referralPromoUsed || b.promoCode == "REFBONUS500"
            )
        )

        db.notificationDao().insert(
            AppNotification(
                title = "Order placed",
                body = "Order $oid is confirmed. Delivery slot: ${input.slot}.",
                createdAt = now
            )
        )

        db.cartDao().clear()
        return oid
    }

    fun ordersFlow() = db.orderDao().all()

    fun orderWithItems(oid: String): Flow<Pair<Order, List<OrderItem>>?> =
        combine(db.orderDao().byId(oid), db.orderItemDao().forOrder(oid)) { o, items ->
            o?.let { it to items }
        }

    suspend fun cancelOrder(oid: String) {
        db.orderDao().setStatus(oid, "CANCELLED")
        db.notificationDao().insert(
            AppNotification(
                title = "Order cancelled",
                body = "Order $oid has been cancelled.",
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun buyoutRental(orderId: String, itemId: Long): Int {
        val items = db.orderItemDao().forOrderOnce(orderId)
        val it = items.find { it.id == itemId } ?: return 0
        val remaining = it.price - it.monthly * it.monthsPaid
        val amount = kotlin.math.round(remaining * 0.9f).toInt().coerceAtLeast(0)
        db.orderItemDao().setBuyout(itemId, 12, true)
        val loy = db.loyaltyDao().getOnce() ?: LoyaltyAccount()
        db.loyaltyDao().save(loy.copy(points = loy.points + amount / 100, totalEarned = loy.totalEarned + amount / 100))
        return amount
    }

    fun reviewsFlow(pid: String) = db.reviewDao().forProduct(pid)

    suspend fun addReview(productId: String, userName: String, stars: Int, text: String, photoUri: String?) {
        val verified = db.orderItemDao().purchasedBefore(productId) != null
        db.reviewDao().insert(
            Review(
                productId = productId, userName = userName, stars = stars, text = text,
                photoUri = photoUri, verified = verified, createdAt = System.currentTimeMillis()
            )
        )
    }

    fun consultsFlow() = db.consultDao().all()

    suspend fun bookConsult(date: Long, slot: String, fee: Int) =
        db.consultDao().insert(Consult(date = date, slot = slot, status = "BOOKED", fee = fee, createdAt = System.currentTimeMillis()))

    suspend fun cancelConsult(id: Long) = db.consultDao().setStatus(id, "CANCELLED")
    suspend fun rescheduleConsult(id: Long, date: Long, slot: String) = db.consultDao().reschedule(id, date, slot)

    fun loyaltyFlow(): Flow<LoyaltyAccount> = db.loyaltyDao().get().map { it ?: LoyaltyAccount() }

    suspend fun joinCircle(): String {
        val cur = db.loyaltyDao().getOnce() ?: LoyaltyAccount()
        if (cur.joined) return cur.referralCode
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val part = { (1..4).map { alphabet.random() }.joinToString("") }
        val code = "WN-${part()}-${part()}"
        db.loyaltyDao().save(cur.copy(joined = true, referralCode = code))
        return code
    }

    suspend fun applyReferralCode(code: String): Boolean {
        val ok = code.trim().uppercase().matches(Regex("WN-[A-Z0-9]{4}-[A-Z0-9]{4}"))
        if (!ok) return false
        val cur = db.loyaltyDao().getOnce() ?: LoyaltyAccount()
        if (cur.referralPromoUsed) return false
        if (!cur.joined) joinCircle()
        return true
    }

    fun notificationsFlow() = db.notificationDao().all()
    fun unreadCount() = db.notificationDao().unreadCount()

    suspend fun addNotification(t: String, b: String) =
        db.notificationDao().insert(AppNotification(title = t, body = b, createdAt = System.currentTimeMillis()))

    suspend fun markAllRead() = db.notificationDao().markAllRead()

    fun reelLikeIdsFlow(): Flow<Set<String>> = db.reelDao().likedIds().map { it.toSet() }

    suspend fun toggleReelLike(id: String) {
        val liked = db.reelDao().likedIds().first().contains(id)
        if (liked) db.reelDao().unlike(id) else db.reelDao().insert(ReelLike(id))
    }

    fun recentFlow(): Flow<List<Product>> = db.recentDao().all().map { rv ->
        rv.mapNotNull { db.productDao().byIdOnce(it.productId) }
    }

    suspend fun recordView(pid: String) {
        db.recentDao().insert(RecentView(pid, System.currentTimeMillis()))
        db.recentDao().trim()
    }

    fun getProfile(): UserProfile =
        UserProfile(prefs.getString("name", "") ?: "", prefs.getString("phone", "") ?: "")

    fun saveProfile(p: UserProfile) {
        prefs.edit().putString("name", p.name).putString("phone", p.phone).apply()
    }
}
