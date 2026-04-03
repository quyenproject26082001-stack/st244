package com.cat.cute.callthecat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cat.cute.callthecat.data.local.entity.GameState
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {

    @Query("SELECT * FROM game_state WHERE id = 1")
    fun getGameState(): Flow<GameState?>

    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameStateOnce(): GameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(gameState: GameState)

    @Query("UPDATE game_state SET coins = :coins WHERE id = 1")
    suspend fun updateCoins(coins: Long)

    @Query("UPDATE game_state SET coinsPerClick = :coinsPerClick WHERE id = 1")
    suspend fun updateCoinsPerClick(coinsPerClick: Long)

    @Query("UPDATE game_state SET coinsPerSecond = :coinsPerSecond WHERE id = 1")
    suspend fun updateCoinsPerSecond(coinsPerSecond: Long)

    @Query("UPDATE game_state SET selectedCatId = :catId WHERE id = 1")
    suspend fun updateSelectedCat(catId: Int)
}
