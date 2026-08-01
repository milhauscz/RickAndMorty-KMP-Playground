package cz.cernilovsky.kmp.rickandmorty.runtime

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import coil3.compose.setSingletonImageLoaderFactory
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import cz.cernilovsky.kmp.rickandmorty.core.image.createImageLoader
import cz.cernilovsky.kmp.rickandmorty.core.ui.theme.RickAndMortyTheme
import org.koin.compose.KoinIsolatedContext

/**
 * Binds Compose and Koin to the SDK's isolated container.
 *
 * Wrap character screens from `:feature:characters:ui` in this composable.
 */
@OptIn(InternalRickAndMortyApi::class, InternalRickAndMortyRuntimeApi::class)
@Composable
public fun RickAndMortySdkScope(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    check(RickAndMortySdk.isInitialized) {
        "RickAndMortySdk.initialize(...) must be called before showing any character screen."
    }

    setSingletonImageLoaderFactory { context -> createImageLoader(context) }

    KoinIsolatedContext(RickAndMortySdk.internalContainer) {
        RickAndMortyTheme {
            Box(modifier) {
                content()
            }
        }
    }
}
