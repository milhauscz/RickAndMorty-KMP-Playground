package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults

/**
 * The key is namespaced because `standardUserDefaults` is shared with the whole host application,
 * and an SDK writing an unprefixed `install_id` there would be a collision waiting to happen.
 */
internal class UserDefaultsInstallIdStore(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : InstallIdStore {
    override fun installId(): String {
        userDefaults.stringForKey(KEY_INSTALL_ID)?.let { return it }

        val generated = NSUUID().UUIDString
        userDefaults.setObject(generated, KEY_INSTALL_ID)
        return generated
    }

    private companion object {
        const val KEY_INSTALL_ID = "cz.cernilovsky.kmp.rickandmorty.featureflags.install_id"
    }
}
