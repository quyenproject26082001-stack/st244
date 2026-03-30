package com.ponymaker.avatarcreator.maker.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ponymaker.avatarcreator.maker.data.local.DatabaseProvider
import com.ponymaker.avatarcreator.maker.data.local.entity.ShopUpgrade
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val shopUpgradeDao = db.shopUpgradeDao()
    private val gameStateDao = db.gameStateDao()

    val upgrades: StateFlow<List<ShopUpgrade>> = shopUpgradeDao.getAllUpgrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun tryBuy(upgrade: ShopUpgrade, onNotEnoughCoins: () -> Unit) {
        viewModelScope.launch {
            val state = gameStateDao.getGameStateOnce() ?: return@launch
            if (state.coins < upgrade.price) {
                onNotEnoughCoins()
                return@launch
            }
            gameStateDao.updateCoins(state.coins - upgrade.price)
            shopUpgradeDao.levelUpgrade(upgrade.id)

            if (upgrade.bonusType == "CLICK") {
                gameStateDao.updateCoinsPerClick(state.coinsPerClick + upgrade.bonusAmount)
            } else {
                gameStateDao.updateCoinsPerSecond(state.coinsPerSecond + upgrade.bonusAmount)
            }
        }
    }
}
