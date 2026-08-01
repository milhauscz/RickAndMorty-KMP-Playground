package cz.cernilovsky.kmp.rickandmorty

import android.os.Build
import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

@InternalRickAndMortyApi
public class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

@InternalRickAndMortyApi
public actual fun getPlatform(): Platform = AndroidPlatform()
