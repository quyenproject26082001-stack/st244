package com.cat.cute.callthecat.data.model.custom

import com.cat.cute.callthecat.data.model.custom.ColorModel

data class LayerModel(
    val image: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf(),
    val thumb: String = ""
)