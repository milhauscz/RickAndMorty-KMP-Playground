package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

/** Outcome of a headless character page load ([CharactersLoadType]). */
public data class CharactersPageLoadResult(
    public val characters: List<Character>,
    public val hasMore: Boolean,
    public val hasPrevious: Boolean,
)
