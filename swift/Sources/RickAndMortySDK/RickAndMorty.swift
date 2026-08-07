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
        // Kotlin default args are not exported to ObjC/Swift — pass empty extraModules explicitly.
        RickAndMortySdk.shared.initialize(
            config: RickAndMortySdkConfig.companion.builder()
                .mode(value: .headless)
                .baseUrl(value: baseUrl)
                .build(),
            extraModules: []
        )
    }

    /// Shuts down the SDK graph. Call after disposing any ``CharactersClient`` instances.
    public static func shutdown() {
        RickAndMortySdk.shared.shutdown()
    }
}
