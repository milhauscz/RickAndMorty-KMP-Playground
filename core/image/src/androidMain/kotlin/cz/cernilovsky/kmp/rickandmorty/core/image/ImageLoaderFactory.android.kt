package cz.cernilovsky.kmp.rickandmorty.core.image

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath

internal actual fun imageCacheDirectory(context: PlatformContext): Path =
    context.cacheDir
        .resolve("image_cache")
        .absolutePath
        .toPath()
