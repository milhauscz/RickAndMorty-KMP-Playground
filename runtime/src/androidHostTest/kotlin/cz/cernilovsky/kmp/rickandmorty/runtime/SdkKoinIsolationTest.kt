package cz.cernilovsky.kmp.rickandmorty.runtime

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.domain.CharactersRepository
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class SdkKoinIsolationTest {
    private data class HostService(
        val name: String,
    )

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        RickAndMortySdk.shutdown()
        runCatching { stopKoin() }
    }

    @Test
    fun initializes_whenHostAppAlreadyStartedItsOwnKoin() {
        val hostKoin =
            startKoin {
                modules(module { single { HostService("host") } })
            }.koin

        RickAndMortySdk.initialize(context)

        assertTrue(RickAndMortySdk.isInitialized)
        assertEquals("host", hostKoin.get<HostService>().name)
    }

    @Test
    fun hostCanStartItsOwnKoin_afterSdkIsInitialized() {
        RickAndMortySdk.initialize(context)

        val hostKoin =
            startKoin {
                modules(module { single { HostService("host") } })
            }.koin

        assertEquals("host", hostKoin.get<HostService>().name)
    }

    @Test
    fun sdkDefinitionsAreInvisibleToTheHostContainer() {
        RickAndMortySdk.initialize(context)

        val hostKoin = startKoin { }.koin

        assertFailsWith<Exception> { hostKoin.get<CharactersRepository>() }
    }

    @Test
    fun rejectsDoubleInitialization_andRecoversAfterShutdown() {
        RickAndMortySdk.initialize(context)

        assertFailsWith<IllegalStateException> { RickAndMortySdk.initialize(context) }

        RickAndMortySdk.shutdown()
        assertFalse(RickAndMortySdk.isInitialized)

        RickAndMortySdk.initialize(context)
        assertTrue(RickAndMortySdk.isInitialized)
    }
}
