package com.kurupdevs.woodnest.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name")
    fun all(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id=:id")
    fun byId(id: String): Flow<Product?>

    @Query("SELECT * FROM products WHERE id=:id")
    suspend fun byIdOnce(id: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%'||:q||'%' OR category LIKE '%'||:q||'%'")
    fun search(q: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE region=:region")
    fun byRegion(region: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<Product>)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun all(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE productId=:pid AND isRental=:rental LIMIT 1")
    suspend fun find(pid: String, rental: Boolean): CartItem?

    @Insert
    suspend fun insert(i: CartItem): Long

    @Update
    suspend fun update(i: CartItem)

    @Query("DELETE FROM cart_items WHERE id=:id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites")
    fun all(): Flow<List<Favorite>>

    @Query("SELECT productId FROM favorites")
    fun ids(): Flow<List<String>>

    @Query("SELECT * FROM favorites WHERE productId=:pid")
    suspend fun byId(pid: String): Favorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(f: Favorite)

    @Query("DELETE FROM favorites WHERE productId=:pid")
    suspend fun delete(pid: String)

    @Query("UPDATE favorites SET alertsOn=:on WHERE productId=:pid")
    suspend fun setAlerts(pid: String, on: Boolean)
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recent_views ORDER BY viewedAt DESC LIMIT 20")
    fun all(): Flow<List<RecentView>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(v: RecentView)

    @Query("DELETE FROM recent_views WHERE productId NOT IN (SELECT productId FROM recent_views ORDER BY viewedAt DESC LIMIT 20)")
    suspend fun trim()
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses ORDER BY isDefault DESC")
    fun all(): Flow<List<Address>>

    @Query("SELECT * FROM addresses WHERE isDefault=1 LIMIT 1")
    fun defaultAddress(): Flow<Address?>

    @Query("SELECT * FROM addresses WHERE isDefault=1 LIMIT 1")
    suspend fun defaultOnce(): Address?

    @Insert
    suspend fun insert(a: Address): Long

    @Update
    suspend fun update(a: Address)

    @Query("DELETE FROM addresses WHERE id=:id")
    suspend fun delete(id: Long)

    @Query("UPDATE addresses SET isDefault=0")
    suspend fun clearDefault()

    @Query("UPDATE addresses SET isDefault=1 WHERE id=:id")
    suspend fun setDefault(id: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY placedAt DESC")
    fun all(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id=:id")
    fun byId(id: String): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id=:id")
    suspend fun byIdOnce(id: String): Order?

    @Insert
    suspend fun insert(o: Order)

    @Query("UPDATE orders SET status=:s WHERE id=:id")
    suspend fun setStatus(id: String, s: String)
}

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId=:oid")
    fun forOrder(oid: String): Flow<List<OrderItem>>

    @Query("SELECT * FROM order_items WHERE orderId=:oid")
    suspend fun forOrderOnce(oid: String): List<OrderItem>

    @Insert
    suspend fun insertAll(items: List<OrderItem>)

    @Query("UPDATE order_items SET monthsPaid=:m, boughtOut=:b WHERE id=:id")
    suspend fun setBuyout(id: Long, m: Int, b: Boolean)

    @Query("SELECT * FROM order_items oi JOIN orders o ON o.id=oi.orderId WHERE oi.productId=:pid AND o.status!='CANCELLED' LIMIT 1")
    suspend fun purchasedBefore(pid: String): OrderItem?
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId=:pid ORDER BY createdAt DESC")
    fun forProduct(pid: String): Flow<List<Review>>

    @Insert
    suspend fun insert(r: Review)
}

@Dao
interface ConsultDao {
    @Query("SELECT * FROM consults ORDER BY date")
    fun all(): Flow<List<Consult>>

    @Insert
    suspend fun insert(c: Consult): Long

    @Query("UPDATE consults SET status=:s WHERE id=:id")
    suspend fun setStatus(id: Long, s: String)

    @Query("UPDATE consults SET date=:d, slot=:sl WHERE id=:id")
    suspend fun reschedule(id: Long, d: Long, sl: String)
}

@Dao
interface LoyaltyDao {
    @Query("SELECT * FROM loyalty WHERE id=1")
    fun get(): Flow<LoyaltyAccount?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(a: LoyaltyAccount)

    @Query("SELECT * FROM loyalty WHERE id=1 LIMIT 1")
    suspend fun getOnce(): LoyaltyAccount?
}

@Dao
interface PriceDao {
    @Query("SELECT * FROM price_history WHERE productId=:pid ORDER BY changedAt DESC")
    suspend fun forProduct(pid: String): List<PriceHistory>

    @Insert
    suspend fun insert(p: PriceHistory)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun all(): Flow<List<AppNotification>>

    @Insert
    suspend fun insert(n: AppNotification)

    @Query("UPDATE notifications SET `read`=1")
    suspend fun markAllRead()

    @Query("SELECT COUNT(*) FROM notifications WHERE `read`=0")
    fun unreadCount(): Flow<Int>
}

@Dao
interface ReelDao {
    @Query("SELECT reelId FROM reel_likes")
    fun likedIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(l: ReelLike)

    @Query("DELETE FROM reel_likes WHERE reelId=:id")
    suspend fun unlike(id: String)
}
