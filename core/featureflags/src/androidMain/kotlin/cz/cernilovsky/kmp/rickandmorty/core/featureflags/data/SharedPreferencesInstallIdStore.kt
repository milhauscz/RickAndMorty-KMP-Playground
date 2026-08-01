package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import android.content.Context
import java.util.UUID

/**
 * Its own preferences file rather than the host's default one: the SDK writing into a partner
 * application's shared preferences is the kind of surprise that gets an SDK removed.
 */
internal class SharedPreferencesInstallIdStore(
    context: Context,
) : InstallIdStore {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Two threads asking on a cold start would otherwise generate two ids and persist the later one,
    // moving the installation between buckets within a single launch.
    @Synchronized
    override fun installId(): String {
        preferences.getString(KEY_INSTALL_ID, null)?.let { return it }

        val generated = UUID.randomUUID().toString()
        preferences.edit().putString(KEY_INSTALL_ID, generated).apply()
        return generated
    }

    private companion object {
        const val PREFERENCES_NAME = "cz.cernilovsky.kmp.rickandmorty.featureflags"
        const val KEY_INSTALL_ID = "install_id"
    }
}
