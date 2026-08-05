package com.cozynotes.app.model

/**
 * Pixar-style avatar options. New avatars can be added here — every screen that
 * shows an avatar reads from this enum, so adding a case is the only step required.
 */
enum class Avatar(val id: String, val displayName: String) {
    BOY("boy", "Boy"),
    GIRL("girl", "Girl");

    companion object {
        fun fromId(id: String?): Avatar = entries.find { it.id == id } ?: GIRL
    }
}
