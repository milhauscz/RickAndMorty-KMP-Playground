package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.rolloutBucket
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultFeatureFlagsTest {
    private val flag = FeatureFlag(key = "new_behaviour", defaultEnabled = false)
    private val installId = "install-a"
    private val dataSource = FakeFeatureFlagsDataSource()

    @Test
    fun `falls back to the compile-time default before any refresh`() {
        val flags = DefaultFeatureFlags(installId, dataSource = dataSource)

        assertFalse(flags.isEnabled(flag))
        assertTrue(flags.isEnabled(flag.copy(defaultEnabled = true)))
    }

    @Test
    fun `keeps the default when the remote config never mentions the flag`() =
        runTest {
            dataSource.config = mapOf("some_other_flag" to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val flags = DefaultFeatureFlags(installId, dataSource = dataSource)

            flags.refresh()

            assertFalse(flags.isEnabled(flag))
        }

    @Test
    fun `enables a flag rolled out to everyone`() =
        runTest {
            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val flags = DefaultFeatureFlags(installId, dataSource = dataSource)

            flags.refresh()

            assertTrue(flags.isEnabled(flag))
        }

    @Test
    fun `disabled beats any rollout percentage`() =
        runTest {
            // The kill switch has to work regardless of who is in the bucket, or it is not one.
            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 100))
            val flags = DefaultFeatureFlags(installId, dataSource = dataSource)

            flags.refresh()

            assertFalse(flags.isEnabled(flag))
        }

    @Test
    fun `rollout percentage decides membership at the bucket boundary`() =
        runTest {
            val bucket = rolloutBucket(installId, flag.key)

            // A percentage one above this installation's bucket includes it; one equal to it does
            // not. Anything vaguer than this passes with an implementation that ignores the bucket.
            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket + 1))
            val included = DefaultFeatureFlags(installId, dataSource = dataSource)
            included.refresh()
            assertTrue(included.isEnabled(flag), "bucket $bucket should be inside a ${bucket + 1}% rollout")

            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket))
            val excluded = DefaultFeatureFlags(installId, dataSource = dataSource)
            excluded.refresh()
            assertFalse(excluded.isEnabled(flag), "bucket $bucket should be outside a $bucket% rollout")
        }

    @Test
    fun `host override wins over the remote config`() =
        runTest {
            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 0))
            val flags =
                DefaultFeatureFlags(
                    installId = installId,
                    overrides = mapOf(flag.key to true),
                    dataSource = dataSource,
                )

            flags.refresh()

            assertTrue(flags.isEnabled(flag))
        }

    @Test
    fun `a failed refresh leaves the previous configuration in place`() =
        runTest {
            dataSource.config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val flags = DefaultFeatureFlags(installId, dataSource = dataSource)
            flags.refresh()

            dataSource.error = DataError.Remote.NO_INTERNET
            val result = flags.refresh()

            assertEquals(Result.Error(DataError.Remote.NO_INTERNET), result)
            assertTrue(flags.isEnabled(flag), "a network failure must not silently revert behaviour")
        }

    @Test
    fun `refresh succeeds without a remote source`() =
        runTest {
            val flags = DefaultFeatureFlags(installId, dataSource = null)

            assertEquals(Result.Success(Unit), flags.refresh())
            assertEquals(0, dataSource.fetchCount)
        }
}
