package com.cat.cute.callthecat.data.model

data class LanguageModel(
    val code: String,
    val name: String,
    val flag: Int,
    var activate: Boolean = false
)
