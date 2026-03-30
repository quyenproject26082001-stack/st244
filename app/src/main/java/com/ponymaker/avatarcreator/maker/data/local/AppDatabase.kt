package com.ponymaker.avatarcreator.maker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ponymaker.avatarcreator.maker.data.local.dao.CatContactDao
import com.ponymaker.avatarcreator.maker.data.local.dao.GameStateDao
import com.ponymaker.avatarcreator.maker.data.local.dao.ShopUpgradeDao
import com.ponymaker.avatarcreator.maker.data.local.dao.UserDao
import com.ponymaker.avatarcreator.maker.data.local.entity.CatContact
import com.ponymaker.avatarcreator.maker.data.local.entity.GameState
import com.ponymaker.avatarcreator.maker.data.local.entity.ShopUpgrade
import com.ponymaker.avatarcreator.maker.data.local.entity.User

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
