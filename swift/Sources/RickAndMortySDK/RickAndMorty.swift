import Foundation

/// Convenience entry points for headless SDK bootstrap from Swift.
public enum RickAndMorty {
    /// Initializes the SDK in headless mode (default for Swift hosts).
    ///
    /// - Parameters:
    ///   - baseUrl: Rick and Morty API base URL.
    public static func initializeHeadless(
        baseUrl: String = "https://rickandmortyapi.com/api"
    ) {
        RickAndMortySdkIosKt.initialize(
            config: RickAndMortySdkConfig.companion.builder()
                .mode(mode: .headless)
                .baseUrl(value: baseUrl)
                .build()
        )
    }

    /// Shuts down the SDK graph. Call after disposing any ``CharactersClient`` instances.
    public static func shutdown() {
        RickAndMortySdk.shared.shutdown()
    }
}
