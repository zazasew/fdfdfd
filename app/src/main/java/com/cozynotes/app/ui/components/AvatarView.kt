package com.cozynotes.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cozynotes.app.R
import com.cozynotes.app.model.Avatar

/**
 * Renders a user's chosen avatar as a cropped circular portrait. Backed by real
 * illustration assets in res/drawable — add a new case here (plus a drawable and
 * an [Avatar] enum entry) to introduce another avatar option later.
 */
@Composable
fun AvatarView(
    avatar: Avatar,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val drawableRes = when (avatar) {
        Avatar.BOY -> R.drawable.avatar_boy
        Avatar.GIRL -> R.drawable.avatar_girl
    }

    Image(
        painter = painterResource(id = drawableRes),
        contentDescription = "${avatar.displayName} avatar",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
    )
}
