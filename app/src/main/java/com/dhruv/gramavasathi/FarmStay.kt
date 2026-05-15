package com.dhruv.gramavasathi

import com.google.firebase.firestore.PropertyName

data class FarmStay(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val hostName: String = "",
    val hostDescription: String = "",
    val price: Long = 0,
    val rating: Double = 0.0,
    @get:PropertyName("coverImageUrl")
    @set:PropertyName("coverImageUrl")
    var coverImageUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val galleryImages: List<String> = emptyList(),
    val activities: List<String> = emptyList()
)
