package com.ponymaker.avatarcreator.maker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cat_contacts")
data class CatContact(
    @PrimaryKey
    val id: Int,
    val name: String,
    val imageRes: String,
    val price: Long,
    val isUnlocked: Boolean = false,
    val isSelected: Boolean = false
)
