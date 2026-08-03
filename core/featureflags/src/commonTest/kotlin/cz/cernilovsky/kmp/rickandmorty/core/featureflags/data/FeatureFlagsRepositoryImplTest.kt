package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.domain.DataError
import cz.cernilovsky.kmp.rickandmorty.core.domain.Result
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.data.local.FeatureFlagConfigEntity
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.FeatureFlag
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.rolloutBucket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureFlagsRepositoryImplTest {
    private val flag = FeatureFlag.CharacterDetailAutoRefresh
    private val installId = "install-a"

    private fun TestScope.repository(
        local: FakeFeatureFlagsRoomDataSource = FakeFeatureFlagsRoomDataSource(),
        remote: FakeFeatureFlagsDataSource? = FakeFeatureFlagsDataSource(),
        overrides: Map<String, Boolean> = emptyMap(),
    ): Pair<FeatureFlagsRepositoryImpl, FakeFeatureFlagsRoomDataSource> {
        val room = local
        val flags =
            FeatureFlagsRepositoryImpl(
                localDataSource = room,
                remoteDataSource = remote,
                installId = installId,
                overrides = overrides,
                scope = backgroundScope,
            )
        return flags to room
    }

    @Test
    fun `falls back to the compile-time default before any cache`() =
        runTest(UnconfinedTestDispatcher()) {
            val (flags, _) = repository()

            assertFalse(flags.isEnabled(flag))
        }

    @Test
    fun `uses the Room cache on cold start when remote fails`() =
        runTest(UnconfinedTestDispatcher()) {
            val local = FakeFeatureFlagsRoomDataSource()
            local.setCached(mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)))
            val remote = FakeFeatureFlagsDataSource().apply { error = DataError.Remote.NO_INTERNET }
            val (flags, _) = repository(local = local, remote = remote)

            assertTrue(flags.isEnabled(flag))
        }

    @Test
    fun `keeps the default when the remote config never mentions the flag`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote =
                FakeFeatureFlagsDataSource(
                    config = mapOf("some_other_flag" to RemoteFlagConfig(enabled = true, rolloutPercent = 100)),
                )
            val (flags, _) = repository(remote = remote)

            assertFalse(flags.isEnabled(flag))
        }

    @Test
    fun `enables a flag rolled out to everyone and persists it`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)),
                )
            val (flags, local) = repository(remote = remote)

            assertTrue(flags.isEnabled(flag))
            assertEquals(remote.config, local.snapshot())
        }

    @Test
    fun `disabled beats any rollout percentage`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 100)),
                )
            val (flags, _) = repository(remote = remote)

            assertFalse(flags.isEnabled(flag))
        }

    @Test
    fun `rollout percentage decides membership at the bucket boundary`() =
        runTest(UnconfinedTestDispatcher()) {
            val bucket = rolloutBucket(installId, flag.key)

            val includedRemote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket + 1)),
                )
            val (included, _) = repository(remote = includedRemote)
            assertTrue(included.isEnabled(flag), "bucket $bucket should be inside a ${bucket + 1}% rollout")

            val excludedRemote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = bucket)),
                )
            val (excluded, _) = repository(remote = excludedRemote)
            assertFalse(excluded.isEnabled(flag), "bucket $bucket should be outside a $bucket% rollout")
        }

    @Test
    fun `host override wins over the remote config`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = false, rolloutPercent = 0)),
                )
            val (flags, _) = repository(remote = remote, overrides = mapOf(flag.key to true))

            assertTrue(flags.isEnabled(flag))
        }

    @Test
    fun `a failed refresh leaves the previous configuration in place`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote =
                FakeFeatureFlagsDataSource(
                    config = mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)),
                )
            val (flags, _) = repository(remote = remote)
            assertTrue(flags.isEnabled(flag))

            remote.error = DataError.Remote.NO_INTERNET
            val result = flags.refresh()

            assertEquals(Result.Error(DataError.Remote.NO_INTERNET), result)
            assertTrue(flags.isEnabled(flag), "a network failure must not silently revert behaviour")
        }

    @Test
    fun `a failed refresh keeps the Room cache and does not clear it`() =
        runTest(UnconfinedTestDispatcher()) {
            val local = FakeFeatureFlagsRoomDataSource()
            local.setCached(mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)))
            val remote = FakeFeatureFlagsDataSource().apply { error = DataError.Remote.NO_INTERNET }
            val (flags, _) = repository(local = local, remote = remote)

            assertEquals(
                mapOf(flag.key to RemoteFlagConfig(enabled = true, rolloutPercent = 100)),
                local.snapshot(),
            )
            assertTrue(flags.isEnabled(flag))
        }

    @Test
    fun `refresh succeeds without a remote source`() =
        runTest(UnconfinedTestDispatcher()) {
            val remote = FakeFeatureFlagsDataSource()
            val (flags, _) = repository(remote = null)

            assertEquals(Result.Success(Unit), flags.refresh())
            assertEquals(0, remote.fetchCount)
        }
}

internal class FakeFeatureFlagsRoomDataSource : FeatureFlagsRoomDataSource {
    private val entities = MutableStateFlow<List<FeatureFlagConfigEntity>>(emptyList())

    fun setCached(config: Map<String, RemoteFlagConfig>) {
        entities.value =
            config.map { (key, value) ->
                FeatureFlagConfigEntity(key, value.enabled, value.rolloutPercent)
            }
    }

    fun snapshot(): Map<String, RemoteFlagConfig> =
        entities.value.associate { it.key to RemoteFlagConfig(it.enabled, it.rolloutPercent) }

    override fun observeAll(): Flow<List<FeatureFlagConfigEntity>> = entities

    override suspend fun getAll(): List<FeatureFlagConfigEntity> = entities.value

    override suspend fun upsertAll(configs: List<FeatureFlagConfigEntity>) {
        entities.value = configs
    }

    override suspend fun deleteAll() {
        entities.value = emptyList()
    }

    override suspend fun replaceAll(configs: List<FeatureFlagConfigEntity>) {
        deleteAll()
        if (configs.isNotEmpty()) {
            upsertAll(configs)
        }
    }
}
