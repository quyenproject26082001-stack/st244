package com.ponymaker.avatarcreator.maker.data.model.pride

data class PrideFlagModel(
    val id: Int,
    val name: String,
    val assetPath: String,
    var isSelected: Boolean = false,
    val customColors: List<Int>? = null
)
