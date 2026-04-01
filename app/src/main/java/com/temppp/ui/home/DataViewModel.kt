package com.temppp.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.temppp.data.local.DatabaseProvider
import com.temppp.data.local.entity.CatContact
import com.temppp.data.local.entity.GameState
import com.temppp.data.local.entity.ShopUpgrade
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)

    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress

    private val _allData = MutableStateFlow<List<Any>>(emptyList())
    val allData: StateFlow<List<Any>> = _allData

    fun ensureData(context: Context) {
        viewModelScope.launch {
            _loadingProgress.value = 0.1f

            // Seed game state
            if (db.gameStateDao().getGameStateOnce() == null) {
                db.gameStateDao().insertOrUpdate(GameState())
            }
            _loadingProgress.value = 0.4f

            // Seed cats
            if (db.catContactDao().getCount() == 0) {
                db.catContactDao().insertAll(defaultCats())
            }
            _loadingProgress.value = 0.7f

            // Seed shop upgrades
            if (db.shopUpgradeDao().getCount() == 0) {
                db.shopUpgradeDao().insertAll(defaultUpgrades())
            }
            _loadingProgress.value = 1f

            delay(300)
            _allData.value = listOf("ready")
        }
    }

    private fun defaultCats(): List<CatContact> = listOf(
        CatContact(1, "Oia",    "cat_glasses", 0,      isUnlocked = true,  isSelected = true),
        CatContact(2, "Snowy",  "cat_white",   20_000, isUnlocked = false, isSelected = false),
        CatContact(3, "Rusty",  "cat_orange",  20_000, isUnlocked = false, isSelected = false),
        CatContact(4, "Grumpy", "cat_grumpy",  40_000, isUnlocked = false, isSelected = false),
        CatContact(5, "Cap",    "cat_cap",     40_000, isUnlocked = false, isSelected = false),
        CatContact(6, "Luna",   "cat_luna",    40_000, isUnlocked = false, isSelected = false),
        CatContact(7, "Tiny",   "cat_tiny",    20_000, isUnlocked = false, isSelected = false),
        CatContact(8, "Pearl",  "cat_pearl",   40_000, isUnlocked = false, isSelected = false)
    )

    private fun defaultUpgrades(): List<ShopUpgrade> = listOf(
        ShopUpgrade(1,  "Spark began",      "+5 coins/second",    "SECOND", 5L,          50L),
        ShopUpgrade(2,  "Single click",     "+10 coins/click",    "CLICK",  10L,         100L),
        ShopUpgrade(3,  "Energy charge",    "+25 coins/second",   "SECOND", 25L,         1_500L),
        ShopUpgrade(4,  "Double click",     "+100 coins/click",   "CLICK",  100L,        10_000L),
        ShopUpgrade(5,  "Sharp jerk",       "+500 coins/second",  "SECOND", 500L,        100_000L),
        ShopUpgrade(6,  "Strong click",     "+1K coins/click",    "CLICK",  1_000L,      1_000_000L),
        ShopUpgrade(7,  "Powerful impulse", "+1.5K coins/second", "SECOND", 1_500L,      15_000_000L),
        ShopUpgrade(8,  "Powerful click",   "+5K coins/click",    "CLICK",  5_000L,      150_000_000L),
        ShopUpgrade(9,  "Golden touch",     "+1M coins/second",   "SECOND", 1_000_000L,  1_000_000_000L),
        ShopUpgrade(10, "Mighty click",     "+10M coins/click",   "CLICK",  10_000_000L, 100_000_000_000L),
        ShopUpgrade(11, "Shock wave",       "+200M coins/second", "SECOND", 200_000_000L,1_000_000_000_000L),
        ShopUpgrade(12, "Release click",    "+200M coins/click",  "CLICK",  200_000_000L,10_000_000_000_000L)
    )
}
