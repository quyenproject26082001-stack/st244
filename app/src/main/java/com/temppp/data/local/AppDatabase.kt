package com.temppp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.temppp.data.local.dao.CatContactDao
import com.temppp.data.local.dao.GameStateDao
import com.temppp.data.local.dao.ShopUpgradeDao
import com.temppp.data.local.dao.UserDao
import com.temppp.data.local.entity.CatContact
import com.temppp.data.local.entity.GameState
import com.temppp.data.local.entity.ShopUpgrade
import com.temppp.data.local.entity.User

@Database(
    entities = [User::class, GameState::class, CatContact::class, ShopUpgrade::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gameStateDao(): GameStateDao
    abstract fun catContactDao(): CatContactDao
    abstract fun shopUpgradeDao(): ShopUpgradeDao
}
