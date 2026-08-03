package cz.cernilovsky.kmp.rickandmorty.runtime

/** How the host intends to consume the SDK. */
public enum class SdkMode {
    /** Data layer only — repositories and use cases. No Compose screens. */
    Headless,

    /**
     * Compose character screens. Use `RickAndMortySdk.initializeWidget(...)` from
     * `:feature:characters:ui` so the UI Koin module is registered automatically.
     */
    Widget,
}
