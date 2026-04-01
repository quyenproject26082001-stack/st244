package com.temppp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.temppp.data.local.DatabaseProvider
import com.temppp.data.local.entity.CatContact
import com.temppp.data.local.entity.GameState
import com.temppp.data.local.entity.ShopUpgrade
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val gameStateDao = db.gameStateDao()
    private val catContactDao = db.catContactDao()
    private val shopUpgradeDao = db.shopUpgradeDao()

    private val _coins = MutableStateFlow(0L)
    val coins: StateFlow<Long> = _coins

    private val _coinsPerClick = MutableStateFlow(10L)
    val coinsPerClick: StateFlow<Long> = _coinsPerClick

    private val _coinsPerSecond = MutableStateFlow(0L)
    val coinsPerSecond: StateFlow<Long> = _coinsPerSecond

    private val _selectedCat = MutableStateFlow<CatContact?>(null)
    val selectedCat: StateFlow<CatContact?> = _selectedCat

    private val _clickBoostActive = MutableStateFlow(false)
    val clickBoostActive: StateFlow<Boolean> = _clickBoostActive

    private val _passiveBoostActive = MutableStateFlow(false)
    val passiveBoostActive: StateFlow<Boolean> = _passiveBoostActive

    private val _passiveBoostProgress = MutableStateFlow(0f)
    val passiveBoostProgress: StateFlow<Float> = _passiveBoostProgress

    private val _boostProgress = MutableStateFlow(0f)
    val boostProgress: StateFlow<Float> = _boostProgress

    private val _tapProgress = MutableStateFlow(0f)
    val tapProgress: StateFlow<Float> = _tapProgress

    companion object {
        private const val TAP_THRESHOLD = 0.6875f  // mark at 176/256 of bar
        private const val TAP_INCREASE  = 0.15f    // per tap
        private const val TAP_DECAY     = 0.025f   // per 100ms → drains in ~4s
    }

    val upgrades: StateFlow<List<ShopUpgrade>> = shopUpgradeDao.getAllUpgrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cats: StateFlow<List<CatContact>> = catContactDao.getAllCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var clickBoostJob: Job? = null
    private var passiveBoostJob: Job? = null

    init {
        viewModelScope.launch {
            seedDefaultData()
            loadGameState()
            startPassiveIncome()
            startTapDecay()
        }
    }

    private suspend fun seedDefaultData() {
        if (catContactDao.getCount() == 0) {
            catContactDao.insertAll(defaultCats())
        }
        if (shopUpgradeDao.getCount() == 0) {
            shopUpgradeDao.insertAll(defaultUpgrades())
        }
        if (gameStateDao.getGameStateOnce() == null) {
            gameStateDao.insertOrUpdate(GameState())
        }
    }

    private suspend fun loadGameState() {
        val state = gameStateDao.getGameStateOnce() ?: GameState()
        _coins.value = state.coins
        _coinsPerClick.value = state.coinsPerClick
        _coinsPerSecond.value = state.coinsPerSecond
        _selectedCat.value = catContactDao.getSelectedCat()
    }

    fun currentClickValue(): Long {
        val tapMultiplier   = if (_tapProgress.value >= TAP_THRESHOLD) 2L else 1L
        val boostMultiplier = if (_clickBoostActive.value) 2L else 1L
        return _coinsPerClick.value * tapMultiplier * boostMultiplier
    }

    fun onPhoneClick() {
        _tapProgress.value = (_tapProgress.value + TAP_INCREASE).coerceAtMost(1f)
        val tapMultiplier    = if (_tapProgress.value >= TAP_THRESHOLD) 2L else 1L
        val boostMultiplier  = if (_clickBoostActive.value) 2L else 1L
        _coins.value += _coinsPerClick.value * tapMultiplier * boostMultiplier
    }

    private fun startTapDecay() {
        viewModelScope.launch {
            while (true) {
                delay(100)
                if (_tapProgress.value > 0f) {
                    _tapProgress.value = (_tapProgress.value - TAP_DECAY).coerceAtLeast(0f)
                }
            }
        }
    }

    fun activateClickBoost() {
        _clickBoostActive.value = true
        _boostProgress.value = 1f
        clickBoostJob?.cancel()
        clickBoostJob = viewModelScope.launch {
            val duration = 30_000L
            val start = System.currentTimeMillis()
            while (true) {
                delay(100)
                val elapsed = System.currentTimeMillis() - start
                _boostProgress.value = 1f - (elapsed.toFloat() / duration)
                if (elapsed >= duration) {
                    _clickBoostActive.value = false
                    _boostProgress.value = 0f
                    break
                }
            }
        }
    }

    fun activatePassiveBoost() {
        _passiveBoostActive.value = true
        _passiveBoostProgress.value = 1f
        passiveBoostJob?.cancel()
        passiveBoostJob = viewModelScope.launch {
            val duration = 30_000L
            val start = System.currentTimeMillis()
            while (true) {
                delay(100)
                val elapsed = System.currentTimeMillis() - start
                _passiveBoostProgress.value = 1f - (elapsed.toFloat() / duration)
                if (elapsed >= duration) {
                    _passiveBoostActive.value = false
                    _passiveBoostProgress.value = 0f
                    break
                }
            }
        }
    }

    fun refreshStats() {
        viewModelScope.launch {
            val state = gameStateDao.getGameStateOnce() ?: return@launch
            _coins.value = state.coins
            _coinsPerClick.value = state.coinsPerClick
            _coinsPerSecond.value = state.coinsPerSecond
            _selectedCat.value = catContactDao.getSelectedCat()
        }
    }

    fun saveCoins() {
        viewModelScope.launch {
            gameStateDao.updateCoins(_coins.value)
        }
    }

    private fun startPassiveIncome() {
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (_coinsPerSecond.value > 0) {
                    val multiplier = if (_passiveBoostActive.value) 2L else 1L
                    _coins.value += _coinsPerSecond.value * multiplier
                }
            }
        }
    }

    fun buyUpgrade(upgrade: ShopUpgrade, onNotEnoughCoins: () -> Unit) {
        viewModelScope.launch {
            if (_coins.value < upgrade.price) {
                onNotEnoughCoins()
                return@launch
            }
            _coins.value -= upgrade.price
            gameStateDao.updateCoins(_coins.value)
            shopUpgradeDao.levelUpgrade(upgrade.id)
            if (upgrade.bonusType == "CLICK") {
                _coinsPerClick.value += upgrade.bonusAmount
                gameStateDao.updateCoinsPerClick(_coinsPerClick.value)
            } else {
                _coinsPerSecond.value += upgrade.bonusAmount
                gameStateDao.updateCoinsPerSecond(_coinsPerSecond.value)
            }
        }
    }

    fun tryUnlockAndSelectCat(cat: CatContact, onNotEnoughCoins: () -> Unit) {
        viewModelScope.launch {
            if (!cat.isUnlocked) {
                if (_coins.value < cat.price) {
                    onNotEnoughCoins()
                    return@launch
                }
                _coins.value -= cat.price
                gameStateDao.updateCoins(_coins.value)
                catContactDao.unlockCat(cat.id)
            }
            catContactDao.deselectAll()
            catContactDao.selectCat(cat.id)
            gameStateDao.updateSelectedCat(cat.id)
            _selectedCat.value = catContactDao.getSelectedCat()
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
