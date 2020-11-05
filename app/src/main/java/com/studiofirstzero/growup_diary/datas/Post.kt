package com.studiofirstzero.growup_diary.datas

import com.google.firebase.firestore.FieldValue

data class City(
    val id: String? = null,
    val measures_value : Number? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val createdAt: FieldValue? = null,
)