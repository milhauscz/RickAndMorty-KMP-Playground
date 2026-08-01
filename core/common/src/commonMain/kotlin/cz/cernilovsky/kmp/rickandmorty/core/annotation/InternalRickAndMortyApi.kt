package cz.cernilovsky.kmp.rickandmorty.core.annotation

// Public only because Kotlin's `internal` stops at the module boundary. Types that must be visible
// across modules for wiring (Koin modules, Room entities) but are not part of the compatibility
// contract carry this marker and are excluded from ABI dumps.
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This declaration is internal to the Rick and Morty SDK and may change in any release.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
public annotation class InternalRickAndMortyApi
