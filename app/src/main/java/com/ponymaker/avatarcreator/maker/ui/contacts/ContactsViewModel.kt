package com.ponymaker.avatarcreator.maker.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ponymaker.avatarcreator.maker.data.local.DatabaseProvider
import com.ponymaker.avatarcreator.maker.data.local.entity.CatContact
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DatabaseProvider.getDatabase(application)
    private val catContactDao = db.catContactDao()
    private val gameStateDao = db.gameStateDao()

    val cats: StateFlow<List<CatContact>> = catContactDao.getAllCats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun tryUnlockAndSelect(cat: CatContact, onNotEnoughCoins: () -> Unit) {
        viewModelScope.launch {
            val state = gameStateDao.getGameStateOnce() ?: return@launch
            if (!cat.isUnlocked) {
                if (state.coins < cat.price) {
                    onNotEnoughCoins()
                    return@launch
                }
                gameStateDao.updateCoins(state.coins - cat.price)
                catContactDao.unlockCat(cat.id)
            }
            catContactDao.deselectAll()
            catContactDao.selectCat(cat.id)
            gameStateDao.updateSelectedCat(cat.id)
        }
    }
}
