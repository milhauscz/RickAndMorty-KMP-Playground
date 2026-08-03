package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlagsRepository
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.rolloutBucket
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultFeatureFlagsTest {
    private val flag = FeatureFlag.CharacterDetailAutoRefresh
    private val installId = "install-a"
    private val repository = FakeFeatureFlagsRepository()

    private fun flags(
        repo: FakeFeatureFlagsRepository = repository,
        overrides: Map<String, Boolean> = emptyMap(),
    ): DefaultFeatureFlags =
        DefaultFeatureFlags(
            installId = installId,
            overrides = overrides,
            repository = repo,
        )

    @Test
    fun `falls back to the compile-time default before any refresh`() {
        assertFalse(flags().isEnabled(flag))
    }

    @Test
    fun `uses the Room cache on cold start before remote responds`() =
        runTest {
            repository.cached =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            repository.remoteError = DataError.Remote.NO_INTERNET
            val featureFlags = flags()

            featureFlags.refresh()

            assertTrue(featureFlags.isEnabled(flag))
        }

    @Test
    fun `keeps the default when the remote config never mentions the flag`() =
        runTest {
            repository.remoteConfig =
                mapOf("some_other_flag" to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val featureFlags = flags()

            featureFlags.refresh()

            assertFalse(featureFlags.isEnabled(flag))
        }

    @Test
    fun `enables a flag rolled out to everyone`() =
        runTest {
            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val featureFlags = flags()

            featureFlags.refresh()

            assertTrue(featureFlags.isEnabled(flag))
            assertEquals(repository.remoteConfig, repository.cached)
        }

    @Test
    fun `disabled beats any rollout percentage`() =
        runTest {
            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 100))
            val featureFlags = flags()

            featureFlags.refresh()

            assertFalse(featureFlags.isEnabled(flag))
        }

    @Test
    fun `rollout percentage decides membership at the bucket boundary`() =
        runTest {
            val bucket = rolloutBucket(installId, flag.key)

            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket + 1))
            val included = flags()
            included.refresh()
            assertTrue(included.isEnabled(flag), "bucket $bucket should be inside a ${bucket + 1}% rollout")

            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket))
            val excluded = flags(FakeFeatureFlagsRepository().also { it.remoteConfig = repository.remoteConfig })
            excluded.refresh()
            assertFalse(excluded.isEnabled(flag), "bucket $bucket should be outside a $bucket% rollout")
        }

    @Test
    fun `host override wins over the remote config`() =
        runTest {
            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 0))
            val featureFlags = flags(overrides = mapOf(flag.key to true))

            featureFlags.refresh()

            assertTrue(featureFlags.isEnabled(flag))
        }

    @Test
    fun `a failed refresh leaves the previous configuration in place`() =
        runTest {
            repository.remoteConfig =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            val featureFlags = flags()
            featureFlags.refresh()

            repository.remoteError = DataError.Remote.NO_INTERNET
            val result = featureFlags.refresh()

            assertEquals(Result.Error(DataError.Remote.NO_INTERNET), result)
            assertTrue(featureFlags.isEnabled(flag), "a network failure must not silently revert behaviour")
        }

    @Test
    fun `a failed refresh keeps the Room cache and does not clear it`() =
        runTest {
            repository.cached =
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100))
            repository.remoteError = DataError.Remote.NO_INTERNET
            val featureFlags = flags()

            featureFlags.refresh()

            assertEquals(
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)),
                repository.cached,
            )
            assertTrue(featureFlags.isEnabled(flag))
        }

    @Test
    fun `refresh succeeds without a remote source`() =
        runTest {
            repository.hasRemote = false
            val featureFlags = flags()

            assertEquals(Result.Success(Unit), featureFlags.refresh())
            assertEquals(0, repository.fetchCount)
        }
}

private class FakeFeatureFlagsRepository : FeatureFlagsRepository {
    var cached: Map<String, RemoteFlagConfig> = emptyMap()
    var remoteConfig: Map<String, RemoteFlagConfig> = emptyMap()
    var remoteError: DataError.Remote? = null
    var hasRemote: Boolean = true
    var fetchCount: Int = 0

    override suspend fun getCached(): Map<String, RemoteFlagConfig> = cached

    override suspend fun replaceAll(config: Map<String, RemoteFlagConfig>) {
        cached = config
    }

    override suspend fun fetchRemote(): Result<Map<String, RemoteFlagConfig>, DataError.Remote>? {
        if (!hasRemote) return null
        fetchCount++
        return remoteError?.let { Result.Error(it) } ?: Result.Success(remoteConfig)
    }
}
