package com.dindz.core.data.enums

enum class BuiltInPlaylist(val sortable: Boolean) {
    Favorites(sortable = true),
    Offline(sortable = true),
    Top(sortable = false),
    History(sortable = false)
}
