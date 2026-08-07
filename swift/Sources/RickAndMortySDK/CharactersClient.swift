import Foundation
import KMPNativeCoroutinesAsync

/// Swift-friendly page-load mode (maps to Kotlin ``CharactersLoadType``).
public enum CharactersPageLoad: Sendable {
    /// First page: refresh when cache is stale or filters changed; otherwise read from Room.
    case initial
    /// Next page after the last shown character id.
    case append
    /// Previous page before the first shown character id.
    case prepend

    var kotlin: CharactersLoadType {
        switch self {
        case .initial: .initial
        case .append: .append
        case .prepend: .prepend
        }
    }
}

/// First-party Swift facade over the refined ``CharactersIosBridge`` (`__…` ObjC/Swift API).
///
/// Host apps should use this type to avoid dependency on the KMP-NativeCoroutines library; bridge
/// members are marked `NativeCoroutinesRefined` so coroutine entry points appear as `__…` and stay
/// out of normal Swift autocomplete.
public final class CharactersClient: @unchecked Sendable {
    private let bridge: CharactersIosBridge

    /// Creates a client backed by a new refined bridge instance.
    ///
    /// Requires ``RickAndMorty/initializeHeadless(baseUrl:)`` first.
    public init() {
        self.bridge = CharactersIosBridge.companion.create()
    }

    /// Loads a character page.
    ///
    /// - Parameters:
    ///   - load: Page load mode.
    ///   - filters: Active character filters.
    ///   - anchorCharacterId: Last shown id for append, first shown id for prepend; ignored for initial.
    public func loadCharacters(
        load: CharactersPageLoad,
        filters: CharacterFilters = CharacterFilters.companion.EMPTY,
        anchorCharacterId: Int? = nil
    ) async throws -> CharactersPageLoadResult {
        let kotlinResult = try await asyncFunction(
            for: bridge.__loadCharacters(
                loadType: load.kotlin,
                filters: filters,
                anchorCharacterId: anchorCharacterId.map { KotlinInt(value: Int32($0)) }
            )
        )
        return try KotlinResultMapping.value(kotlinResult)
    }

    /// Observes character detail for `id` as values arrive from Room / refresh.
    public func characterDetail(id: Int) -> AsyncThrowingStream<CharacterDetail?, Swift.Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    let sequence = asyncSequence(for: bridge.__observeCharacterDetail(id: Int32(id)))
                    for try await value in sequence {
                        continuation.yield(value)
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    /// Forces a remote refresh of character detail for `id`.
    public func refreshCharacterDetail(id: Int) async throws {
        let kotlinResult = try await asyncFunction(
            for: bridge.__refreshCharacterDetail(id: Int32(id))
        )
        try KotlinResultMapping.unit(kotlinResult)
    }

    /// Observes the active character filters.
    public func filters() -> AsyncThrowingStream<CharacterFilters, Swift.Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    let sequence = asyncSequence(for: bridge.__observeFilters())
                    for try await value in sequence {
                        continuation.yield(value)
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    /// Updates the active character filters.
    public func setFilters(_ filters: CharacterFilters) async throws {
        try await asyncFunction(for: bridge.__setFilters(filters: filters))
    }

    /// Observes the two-pane selected character id.
    public func selectedCharacterId() -> AsyncThrowingStream<Int?, Swift.Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    let sequence = asyncSequence(for: bridge.__observeSelectedCharacterId())
                    for try await value in sequence {
                        continuation.yield(value.map { Int(truncating: $0) })
                    }
                    continuation.finish()
                } catch {
                    continuation.finish(throwing: error)
                }
            }
            continuation.onTermination = { _ in
                task.cancel()
            }
        }
    }

    /// Updates the two-pane selected character id.
    public func setSelectedCharacterId(_ id: Int?) async throws {
        try await asyncFunction(
            for: bridge.__setSelectedCharacterId(id: id.map { KotlinInt(value: Int32($0)) })
        )
    }

    /// Cancels in-flight bridge coroutines. Call before ``RickAndMorty/shutdown()``.
    public func close() {
        bridge.close()
    }
}
