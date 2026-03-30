package com.ponymaker.avatarcreator.maker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_upgrades")
data class ShopUpgrade(
    @PrimaryKey
    val id: Int,
    val name: String,
    val description: String,
    val bonusType: String,   // "CLICK" or "SECOND"
    val bonusAmount: Long,
    val price: Long,
    val level: Int = 0
)
