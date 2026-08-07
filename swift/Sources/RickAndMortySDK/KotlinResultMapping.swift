import Foundation
import RickAndMortySDKCore

/// Errors surfaced by the Swift facade over the Kotlin SDK.
public enum RickAndMortySDKError: Error, Sendable {
    /// Domain remote failure from `DataError.Remote`.
    case remote(DataErrorRemote)
    /// Unexpected interop / type-mapping failure.
    case unexpected(String)
}

enum KotlinResultMapping {
    /// Unwraps a Kotlin `Result.Success` / `Result.Error` into a Swift value or throw.
    static func value<T>(_ result: Any) throws -> T {
        if let success = result as? ResultSuccess<AnyObject> {
            guard let data = success.data as? T else {
                throw RickAndMortySDKError.unexpected(
                    "Result.Success payload type mismatch (got \(type(of: success.data as Any)))"
                )
            }
            return data
        }
        if let failure = result as? ResultError<AnyObject> {
            if let remote = failure.error as? DataErrorRemote {
                throw RickAndMortySDKError.remote(remote)
            }
            throw RickAndMortySDKError.unexpected(
                "Result.Error payload type mismatch (got \(type(of: failure.error as Any)))"
            )
        }
        throw RickAndMortySDKError.unexpected("Unknown Kotlin Result type \(type(of: result))")
    }

    /// Unwraps a Kotlin `EmptyResult` (`Result<Unit, E>`).
    static func unit(_ result: Any) throws {
        if result is ResultSuccess<KotlinUnit> {
            return
        }
        if let success = result as? ResultSuccess<AnyObject>, success.data is KotlinUnit {
            return
        }
        if let failure = result as? ResultError<AnyObject> {
            if let remote = failure.error as? DataErrorRemote {
                throw RickAndMortySDKError.remote(remote)
            }
            throw RickAndMortySDKError.unexpected(
                "Result.Error payload type mismatch (got \(type(of: failure.error as Any)))"
            )
        }
        throw RickAndMortySDKError.unexpected("Unknown Kotlin EmptyResult type \(type(of: result))")
    }
}
