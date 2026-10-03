package com.kurupdevs.woodnest

import android.app.Application
import com.kurupdevs.woodnest.data.db.AppDatabase
import com.kurupdevs.woodnest.data.db.seedIfEmpty
import com.kurupdevs.woodnest.data.repo.WoodNestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WoodNestApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repo by lazy { WoodNestRepository(database, this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch { database.seedIfEmpty() }
    }
}
