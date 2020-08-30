package com.social.alexanderpowell.amietestproject

data class ImageItem(val id: String,
                     val author: String,
                     val width: Int,
                     val height: Int,
                     val url: String,
                     val downloadUrl: String,
                     val isFavorite: Boolean) {
    var expanded: Boolean = false
}