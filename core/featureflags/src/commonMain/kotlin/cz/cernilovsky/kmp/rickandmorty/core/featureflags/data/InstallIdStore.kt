package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

/**
 * Supplies the identifier a rollout bucket is computed from.
 *
 * It has to survive process death, or an installation would be re-bucketed on every launch and a
 * "10% rollout" would mean "10% of launches" — which is both a different thing and a much worse
 * experience. It deliberately identifies an installation and not a person: nothing about bucketing
 * needs to know who the user is, so it does not ask.
 */
internal interface InstallIdStore {
    fun installId(): String
}
