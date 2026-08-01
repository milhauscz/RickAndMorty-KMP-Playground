package cz.cernilovsky.kmp.rickandmorty

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

@InternalRickAndMortyApi
public interface Platform {
    public val name: String
}

@InternalRickAndMortyApi
public expect fun getPlatform(): Platform
