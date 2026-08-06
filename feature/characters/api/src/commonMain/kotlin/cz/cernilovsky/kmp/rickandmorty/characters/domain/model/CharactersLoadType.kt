package cz.cernilovsky.kmp.rickandmorty.characters.domain.model

/** Headless character-list load mode (not [androidx.paging.LoadType]). */
public enum class CharactersLoadType {
    /** First page: refresh when cache is stale or filters changed; otherwise read from Room. */
    Init,

    /** Next page after [anchorCharacterId] (local window first, then remote). */
    Append,

    /** Previous page before [anchorCharacterId] (local window first, then remote). */
    Prepend,
}
