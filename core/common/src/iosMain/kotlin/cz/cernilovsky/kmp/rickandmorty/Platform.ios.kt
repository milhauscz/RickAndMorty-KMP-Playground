package cz.cernilovsky.kmp.rickandmorty

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi
import platform.UIKit.UIDevice

@InternalRickAndMortyApi
public class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

@InternalRickAndMortyApi
public actual fun getPlatform(): Platform = IOSPlatform()
