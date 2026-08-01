package cz.cernilovsky.kmp.rickandmorty.runtime

import cz.cernilovsky.kmp.rickandmorty.core.annotation.InternalRickAndMortyApi

/**
 * Marks declarations that exist only so sibling artifacts (for example the character UI module)
 * can reach into `:runtime`. Not part of the public compatibility contract.
 */
@InternalRickAndMortyApi
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This declaration is internal to the Rick and Morty SDK and may change in any release.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
public annotation class InternalRickAndMortyRuntimeApi
