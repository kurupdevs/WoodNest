package com.kurupdevs.woodnest.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class, CartItem::class, Favorite::class, RecentView::class,
        Address::class, Order::class, OrderItem::class, Review::class,
        Consult::class, LoyaltyAccount::class, PriceHistory::class,
        AppNotification::class, ReelLike::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentDao(): RecentDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun orderItemDao(): OrderItemDao
    abstract fun reviewDao(): ReviewDao
    abstract fun consultDao(): ConsultDao
    abstract fun loyaltyDao(): LoyaltyDao
    abstract fun priceDao(): PriceDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reelDao(): ReelDao

    companion object {
        @Volatile private var I: AppDatabase? = null

        fun getInstance(c: Context): AppDatabase = I ?: synchronized(this) {
            I ?: Room.databaseBuilder(
                c.applicationContext,
                AppDatabase::class.java,
                "woodnest.db"
            ).fallbackToDestructiveMigration().build().also { I = it }
        }
    }
}

suspend fun AppDatabase.seedIfEmpty() {
    if (productDao().byIdOnce("oslo-sofa") == null) {
        productDao().upsertAll(SeedData.products)
        SeedData.priceSeeds.forEach { priceDao().insert(it) }
        SeedData.reviewSeeds.forEach { reviewDao().insert(it) }
        favoriteDao().insert(
            Favorite("arc-floor-lamp", 5499, true, System.currentTimeMillis())
        )
        notificationDao().insert(
            AppNotification(
                0,
                "Price drop alert",
                "Arc Floor Lamp dropped to ₹4,999 — your favorite is on sale!",
                System.currentTimeMillis(),
                false
            )
        )
    }
}
