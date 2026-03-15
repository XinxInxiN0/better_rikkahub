package com.u4e50.rikkahub.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import com.u4e50.rikkahub.ui.context.LocalSharedTransitionScope

@Composable
fun Modifier.heroAnimation(
    key: Any,
): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedContentScope.current
    return with(sharedTransitionScope) {
        this@heroAnimation.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animatedVisibilityScope
        )
    }
}

