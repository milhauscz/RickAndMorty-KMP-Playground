package cz.cernilovsky.kmp.rickandmorty.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.core.network.NetworkConfig
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SdkConfigurationTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        RickAndMortySdk.shutdown()
    }

    @Test
    fun hostSuppliedBaseUrlReachesTheNetworkLayer() {
        RickAndMortySdk.initialize(
            context,
            RickAndMortySdkConfig
                .builder()
                .baseUrl("https://staging.example.com/api/")
                .build(),
        )

        val networkConfig = requireNotNull(RickAndMortySdk.containerOrNull).koin.get<NetworkConfig>()

        assertEquals("https://staging.example.com/api", networkConfig.baseUrl)
    }

    @Test
    fun defaultsToTheUpstreamApi_withLoggingOff() {
        RickAndMortySdk.initialize(context)

        val networkConfig = requireNotNull(RickAndMortySdk.containerOrNull).koin.get<NetworkConfig>()

        assertEquals(RickAndMortySdkConfig.DEFAULT_BASE_URL, networkConfig.baseUrl)
        assertEquals(false, networkConfig.loggingEnabled)
    }
}
