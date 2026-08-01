package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

/**
 * Every flag the project knows about, declared in one place so that "which flags exist" is a
 * question with an answer rather than a search for `isEnabled(` across the codebase.
 *
 * A flag that has finished rolling out is deleted here and its branch removed from the code. Flags
 * that are never cleaned up are how a codebase ends up with behaviour nobody can reproduce.
 */
public object RickAndMortyFlags {
    /**
     * Makes `observeDetail` fetch the character's locations and episodes in the background when
     * collection starts, instead of only replaying what the database already holds.
     *
     * Off by default: it turns a local read into a network request, and a partner integrating the
     * SDK should opt into that rather than discover it in a traffic graph.
     */
    public val characterDetailAutoRefresh: FeatureFlag =
        FeatureFlag(
            key = "character_detail_auto_refresh",
            defaultEnabled = false,
        )

    public val all: List<FeatureFlag> = listOf(characterDetailAutoRefresh)
}
