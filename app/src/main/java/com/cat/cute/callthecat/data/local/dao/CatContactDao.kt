package com.cat.cute.callthecat.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cat.cute.callthecat.data.local.entity.CatContact
import kotlinx.coroutines.flow.Flow

@Dao
interface CatContactDao {

    @Query("SELECT * FROM cat_contacts ORDER BY id ASC")
    fun getAllCats(): Flow<List<CatContact>>

    @Query("SELECT * FROM cat_contacts ORDER BY id ASC")
    suspend fun getAllCatsOnce(): List<CatContact>

    @Query("SELECT * FROM cat_contacts WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedCat(): CatContact?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cats: List<CatContact>)

    @Query("DELETE FROM cat_contacts")
    suspend fun deleteAll()

    @Query("UPDATE cat_contacts SET price = :price WHERE id = :id")
    suspend fun updatePrice(id: Int, price: Long)

    @Query("UPDATE cat_contacts SET isUnlocked = 1 WHERE id = :catId")
    suspend fun unlockCat(catId: Int)

    @Query("UPDATE cat_contacts SET isUnlocked = 1")
    suspend fun unlockAllCats()

    @Query("UPDATE cat_contacts SET isUnlocked = 0 WHERE id != 1")
    suspend fun lockAllCatsExceptFirst()

    @Query("UPDATE cat_contacts SET isSelected = 0")
    suspend fun deselectAll()

    @Query("UPDATE cat_contacts SET isSelected = 1 WHERE id = :catId")
    suspend fun selectCat(catId: Int)

    @Query("SELECT COUNT(*) FROM cat_contacts")
    suspend fun getCount(): Int
}
