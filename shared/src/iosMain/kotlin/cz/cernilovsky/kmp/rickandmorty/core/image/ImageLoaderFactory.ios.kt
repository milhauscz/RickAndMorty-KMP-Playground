package cz.cernilovsky.kmp.rickandmorty.core.image

import coil3.PlatformContext
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun imageCacheDirectory(context: PlatformContext): Path {
    val cachesPath = NSSearchPathForDirectoriesInDomains(
        directory = NSCachesDirectory,
        domainMask = NSUserDomainMask,
        expandTilde = true,
    ).first() as String
    return "$cachesPath/image_cache".toPath()
}
