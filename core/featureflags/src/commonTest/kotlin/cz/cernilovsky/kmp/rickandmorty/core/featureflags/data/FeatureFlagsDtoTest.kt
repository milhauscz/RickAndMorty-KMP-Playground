package cz.cernilovsky.kmp.rickandmorty.core.featureflags.data

import cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain.RemoteFlagConfig
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeatureFlagsDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses a config document`() {
        val document =
            """
            { "flags": { "character_detail_auto_refresh": { "enabled": true, "rolloutPercent": 25 } } }
            """.trimIndent()

        val config = json.decodeFromString<FeatureFlagsDto>(document).toRemoteConfig()

        assertEquals(
            RemoteFlagConfig(enabled = true, rolloutPercent = 25),
            config["character_detail_auto_refresh"],
        )
    }

    @Test
    fun `an entry without a rollout percentage means everyone`() {
        val document = """{ "flags": { "a_flag": { "enabled": true } } }"""

        val config = json.decodeFromString<FeatureFlagsDto>(document).toRemoteConfig()

        assertEquals(RemoteFlagConfig.FULL_ROLLOUT, config.getValue("a_flag").rolloutPercent)
    }

    @Test
    fun `unknown fields and an empty document are not failures`() {
        // A config authored against a newer build must not break an older one, and an SDK that
        // cannot parse its flags should behave like one with no flags rather than crash.
        val newerDocument = """{ "flags": { "a_flag": { "enabled": true, "audience": "beta" } }, "version": 3 }"""

        assertTrue(json.decodeFromString<FeatureFlagsDto>(newerDocument).toRemoteConfig().containsKey("a_flag"))
        assertTrue(json.decodeFromString<FeatureFlagsDto>("{}").toRemoteConfig().isEmpty())
    }

    @Test
    fun `a percentage outside the range is clamped`() {
        val document =
            """{ "flags": { "low": { "enabled": true, "rolloutPercent": -5 }, """ +
                """"high": { "enabled": true, "rolloutPercent": 250 } } }"""

        val config = json.decodeFromString<FeatureFlagsDto>(document).toRemoteConfig()

        assertEquals(0, config.getValue("low").rolloutPercent)
        assertEquals(RemoteFlagConfig.FULL_ROLLOUT, config.getValue("high").rolloutPercent)
    }
}
