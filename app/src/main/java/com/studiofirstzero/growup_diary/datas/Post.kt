package com.studiofirstzero.growup_diary.datas

import android.os.Parcelable
import com.google.firebase.firestore.FieldValue
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val id: String? = null,
    val measureValue: Number? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null,
) : Parcelable