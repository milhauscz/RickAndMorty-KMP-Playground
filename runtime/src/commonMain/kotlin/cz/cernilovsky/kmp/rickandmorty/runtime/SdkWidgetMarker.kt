package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * Koin marker installed by the character UI module so [SdkMode.Widget] can verify the widget graph
 * is present.
 */
@InternalRickAndMortyApi
public class SdkWidgetMarker
