package com.cat.cute.callthecat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cat.cute.callthecat.data.local.dao.CatContactDao
import com.cat.cute.callthecat.data.local.dao.GameStateDao
import com.cat.cute.callthecat.data.local.dao.ShopUpgradeDao
import com.cat.cute.callthecat.data.local.dao.UserDao
import com.cat.cute.callthecat.data.local.entity.CatContact
import com.cat.cute.callthecat.data.local.entity.GameState
import com.cat.cute.callthecat.data.local.entity.ShopUpgrade
import com.cat.cute.callthecat.data.local.entity.User

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
