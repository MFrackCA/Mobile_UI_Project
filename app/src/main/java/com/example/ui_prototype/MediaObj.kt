package com.example.ui_prototype

// Class for media objects form firebase images/videos
data class MediaObj(
    val title: String?,
    val profileImageResId: Int?,
    val description: String?,
    val mediaUri: String?,
    val mediaType: String?,
    val long: Double?,
    val latitude: Double?,
    val username: String?,
    val locationName: String?
)
