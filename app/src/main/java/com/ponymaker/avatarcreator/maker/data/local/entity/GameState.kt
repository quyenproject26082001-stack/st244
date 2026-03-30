package com.ponymaker.avatarcreator.maker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey
    val id: Int = 1,
    val coins: Long = 0L,
    val coinsPerClick: Long = 10L,
    val coinsPerSecond: Long = 0L,
    val selectedCatId: Int = 1
)
