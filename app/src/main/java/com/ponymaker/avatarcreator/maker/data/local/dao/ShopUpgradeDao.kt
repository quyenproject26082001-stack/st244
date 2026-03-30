package com.ponymaker.avatarcreator.maker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ponymaker.avatarcreator.maker.data.local.entity.ShopUpgrade
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopUpgradeDao {

    @Query("SELECT * FROM shop_upgrades ORDER BY price ASC")
    fun getAllUpgrades(): Flow<List<ShopUpgrade>>

    @Query("SELECT * FROM shop_upgrades ORDER BY price ASC")
    suspend fun getAllUpgradesOnce(): List<ShopUpgrade>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(upgrades: List<ShopUpgrade>)

    @Query("UPDATE shop_upgrades SET level = level + 1, price = price * 10 WHERE id = :upgradeId")
    suspend fun levelUpgrade(upgradeId: Int)

    @Query("SELECT COUNT(*) FROM shop_upgrades")
    suspend fun getCount(): Int
}
