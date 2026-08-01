package cz.cernilovsky.kmp.rickandmorty.core.featureflags.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RolloutBucketTest {
    private val flagKey = "character_detail_auto_refresh"

    @Test
    fun `bucket is stable for the same install id`() {
        val first = rolloutBucket("install-a", flagKey)
        val second = rolloutBucket("install-a", flagKey)

        assertEquals(first, second)
    }

    @Test
    fun `bucket is always within range`() {
        val buckets = (0 until SAMPLE_SIZE).map { rolloutBucket("install-$it", flagKey) }

        assertTrue(buckets.all { it in 0..99 }, "buckets outside 0..99: ${buckets.filterNot { it in 0..99 }}")
    }

    @Test
    fun `different flags bucket the same installation differently`() {
        // Otherwise the first 10% of every rollout would be the same installations every time, and
        // one unlucky group would receive every new behaviour before anyone else.
        val agreements =
            (0 until SAMPLE_SIZE).count { index ->
                rolloutBucket("install-$index", "flag_one") == rolloutBucket("install-$index", "flag_two")
            }

        assertTrue(
            agreements < SAMPLE_SIZE / 10,
            "flags agreed on $agreements of $SAMPLE_SIZE installations, which suggests the key is ignored",
        )
    }

    @Test
    fun `buckets are spread roughly evenly`() {
        // A rollout percentage only means anything if the buckets are uniform: at 10% we want about
        // a tenth of installations, not whatever a badly distributed hash happens to give.
        val belowTen = (0 until SAMPLE_SIZE).count { rolloutBucket("install-$it", flagKey) < 10 }
        val share = belowTen.toDouble() / SAMPLE_SIZE

        assertTrue(share in 0.07..0.13, "expected roughly 10% below bucket 10, got ${share * 100}%")
    }

    private companion object {
        const val SAMPLE_SIZE = 2000
    }
}
