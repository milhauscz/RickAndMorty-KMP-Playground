package cz.cernilovsky.kmp.rickandmorty.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

data class SharedTransitionContext(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

val LocalSharedTransitionContext = compositionLocalOf<SharedTransitionContext?> { null }

fun Modifier.registerSharedElement(key: Any): Modifier =
    composed {
        val context = LocalSharedTransitionContext.current

        if (context != null) {
            with(context.sharedTransitionScope) {
                this@composed.sharedElement(
                    rememberSharedContentState(key = key),
                    animatedVisibilityScope = context.animatedVisibilityScope,
                )
            }
        } else {
            // Do nothing if there is no context (e.g. in @Preview)
            this
        }
    }
