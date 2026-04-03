package com.cat.cute.callthecat.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cat.cute.callthecat.data.local.DatabaseProvider
import com.cat.cute.callthecat.data.local.entity.CatContact
import com.cat.cute.callthecat.data.local.entity.GameState
import com.cat.cute.callthecat.data.local.entity.ShopUpgrade
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
        const val TAP_THRESHOLD = 0.6875f  // mark at 176/256 of bar
        private const val TAP_INCREASE  = 0.065f    // per tap
        private const val TAP_DECAY     = 0.025f   // per 100ms → drains in ~4s
    }

    val upgrades: StateFlow<List<ShopUpgrade>> = shopUpgradeDao.getAllUpgrades()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cats: StateFlow<List<CatContact>> = catContactDao.getAllCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val coinLimit: StateFlow<Long> = cats
        .map { list -> list.filter { !it.isUnlocked && !it.isSelected }.minOfOrNull { it.price } ?: Long.MAX_VALUE }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 100L)

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
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val CAT_VERSION = 2
        if (prefs.getInt("cat_db_version", 0) < CAT_VERSION) {
            catContactDao.deleteAll()
            catContactDao.insertAll(defaultCats())
            prefs.edit().putInt("cat_db_version", CAT_VERSION).apply()
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
        val limit = coinLimit.value
        if (_coins.value >= limit) return
        _coins.value = (_coins.value + _coinsPerClick.value * tapMultiplier * boostMultiplier).coerceAtMost(limit)
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

    fun toggleGodMode(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                _coins.value = 999_999_999_999_999L
                gameStateDao.updateCoins(_coins.value)
                catContactDao.unlockAllCats()
            } else {
                _coins.value = 0L
                gameStateDao.updateCoins(_coins.value)
                catContactDao.lockAllCatsExceptFirst()
            }
            _selectedCat.value = catContactDao.getSelectedCat()
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
                    val limit = coinLimit.value
                    if (_coins.value < limit) {
                        _coins.value = (_coins.value + _coinsPerSecond.value * multiplier).coerceAtMost(limit)
                    }
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
        CatContact(1,  "Oia",     "cat1",  0L,                    isUnlocked = true,  isSelected = true),
        CatContact(2,  "Snowy",   "cat2",  100L,                  isUnlocked = false, isSelected = false),
        CatContact(3,  "Rusty",   "cat3",  500L,                  isUnlocked = false, isSelected = false),
        CatContact(4,  "Grumpy",  "cat4",  1_000L,                isUnlocked = false, isSelected = false),
        CatContact(5,  "Cap",     "cat5",  2_000L,                isUnlocked = false, isSelected = false),
        CatContact(6,  "Luna",    "cat6",  5_000L,                isUnlocked = false, isSelected = false),
        CatContact(7,  "Tiny",    "cat7",  10_000L,               isUnlocked = false, isSelected = false),
        CatContact(8,  "Pearl",   "cat8",  25_000L,               isUnlocked = false, isSelected = false),
        CatContact(9,  "Mochi",   "cat9",  50_000L,               isUnlocked = false, isSelected = false),
        CatContact(10, "Nala",    "cat10", 100_000L,              isUnlocked = false, isSelected = false),
        CatContact(11, "Felix",   "cat11", 250_000L,              isUnlocked = false, isSelected = false),
        CatContact(12, "Cleo",    "cat12", 500_000L,              isUnlocked = false, isSelected = false),
        CatContact(13, "Mango",   "cat13", 1_000_000L,            isUnlocked = false, isSelected = false),
        CatContact(14, "Pixel",   "cat14", 2_500_000L,            isUnlocked = false, isSelected = false),
        CatContact(15, "Nova",    "cat15", 5_000_000L,            isUnlocked = false, isSelected = false),
        CatContact(16, "Cosmo",   "cat16", 10_000_000L,           isUnlocked = false, isSelected = false),
        CatContact(17, "Milo",    "cat17", 25_000_000L,           isUnlocked = false, isSelected = false),
        CatContact(18, "Zara",    "cat18", 50_000_000L,           isUnlocked = false, isSelected = false),
        CatContact(19, "Blaze",   "cat19", 100_000_000L,          isUnlocked = false, isSelected = false),
        CatContact(20, "Sage",    "cat20", 250_000_000L,          isUnlocked = false, isSelected = false),
        CatContact(21, "Echo",    "cat21", 500_000_000L,          isUnlocked = false, isSelected = false),
        CatContact(22, "Frost",   "cat22", 1_000_000_000L,        isUnlocked = false, isSelected = false),
        CatContact(23, "Storm",   "cat23", 2_500_000_000L,        isUnlocked = false, isSelected = false),
        CatContact(24, "Ember",   "cat24", 5_000_000_000L,        isUnlocked = false, isSelected = false),
        CatContact(25, "Atlas",   "cat25", 10_000_000_000L,       isUnlocked = false, isSelected = false),
        CatContact(26, "Jade",    "cat26", 25_000_000_000L,       isUnlocked = false, isSelected = false),
        CatContact(27, "Shadow",  "cat27", 50_000_000_000L,       isUnlocked = false, isSelected = false),
        CatContact(28, "Comet",   "cat28", 100_000_000_000L,      isUnlocked = false, isSelected = false),
        CatContact(29, "Nebula",  "cat29", 250_000_000_000L,      isUnlocked = false, isSelected = false),
        CatContact(30, "Soleil",  "cat30", 500_000_000_000L,      isUnlocked = false, isSelected = false),
        CatContact(31, "Titan",   "cat31", 1_000_000_000_000L,    isUnlocked = false, isSelected = false),
        CatContact(32, "Vega",    "cat32", 2_500_000_000_000L,    isUnlocked = false, isSelected = false),
        CatContact(33, "Zenith",  "cat33", 5_000_000_000_000L,    isUnlocked = false, isSelected = false),
        CatContact(34, "Aurora",  "cat34", 10_000_000_000_000L,   isUnlocked = false, isSelected = false)
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
