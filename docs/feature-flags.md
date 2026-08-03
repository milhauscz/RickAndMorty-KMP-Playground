# Feature flags

Releasing a build and turning a behaviour on are two different events. `:core:featureflags` is what
separates them: code ships dark, gets switched on for a share of installations, and can be switched
off again without a release cycle — which matters more for an SDK than for an app, because turning
something off in an SDK otherwise means asking a partner to ship.

## How a flag is resolved

```
host override  →  remote config (gated by rollout bucket)  →  compile-time default
```

Each step exists for a reason a step above it cannot serve:

1. **Host override.** `RickAndMortySdkConfig.builder().overrideFeatureFlag(key, enabled)`. Absolute,
   including over a kill switch. A partner who cannot pin a behaviour deterministically cannot write
   a test for their own integration, and what they do instead is mock the SDK entirely — at which
   point their tests stop telling either side anything.
2. **Remote config**, fetched once per initialization from `remoteConfigUrl`:

   ```json
   { "flags": { "character_detail_auto_refresh": { "enabled": true, "rolloutPercent": 25 } } }
   ```

   `enabled` and `rolloutPercent` are separate: `enabled: false` is a kill switch that applies
   everywhere at once, while lowering the percentage only stops new installations from joining.
3. **Compile-time default**, declared on the [FeatureFlag] sealed class. This is what applies
   before the local cache has been loaded, when neither cache nor remote mentions the flag, and for
   a flag the backend has never heard of. There is no state in which a flag has no answer.

On each startup the SDK observes the Room cache as a hot [StateFlow] (`SharingStarted.Eagerly`,
because [FeatureFlagsRepository.isEnabled] reads the current value synchronously and nothing collects the flow
for UI). A background refresh then fetches remote config when `remoteConfigUrl` is set. A successful
fetch replaces the cache (and the flow emits); a failed fetch leaves both alone. Host overrides and
compile-time defaults are applied in `isEnabled`, not merged into the Room documents — overrides are
absolute booleans, and a missing remote entry means “use the flag’s default,” not a stored row.
There is no TTL in this sample — a kill switch takes effect on the next successful fetch.

Unknown fields and unknown flag keys are ignored rather than rejected, so a config written against a
newer build does not break an older one. A flag system that fails closed on a config typo turns a
console mistake into an outage.

## Rollout bucketing

```kotlin
rolloutBucket(installId, flagKey) < rolloutPercent
```

The bucket is `FNV-1a(installId + ":" + flagKey) % 100`. Three properties are being bought, and each
rules out something simpler:

- **Stable across launches.** The install id is persisted — `SharedPreferences` on Android,
  `NSUserDefaults` on iOS, both in a namespace of ours rather than the host's. Without persistence a
  "10% rollout" would mean "10% of launches", which is a different and much worse thing.
- **Stable across platforms.** `String.hashCode` is only specified on the JVM, so Android and iOS
  could disagree about who is in the group. FNV-1a is defined by its constants.
- **Independent per flag.** Mixing the flag key into the hash stops the same tenth of installations
  from receiving every new behaviour first.

The install id identifies an installation, not a person. Nothing about bucketing needs to know who
the user is, so it does not ask.

## The flag that is actually wired up

`character_detail_auto_refresh` ([FeatureFlag.CharacterDetailAutoRefresh], or
`RickAndMortyFeatureFlags.CHARACTER_DETAIL_AUTO_REFRESH` for overrides) makes
`GetCharacterDetailUseCase.observe()` fetch the character's locations and episodes in the background as
collection starts, instead of only replaying the local cache. It is off by default because it turns
a local read into network traffic, which a partner should opt into rather than discover in a graph.

`CharacterDetailAutoRefreshTest` runs it both ways: with the flag off nothing is fetched, and with it
on the refresh happens *and* the cached value still arrives first. Both directions are tested because
a flag is a promise about two code paths, and the untested one is what everybody outside the rollout
is running.

## What the SDK exposes

Flag keys live on [FeatureFlag] and are re-exported as string constants in `RickAndMortyFeatureFlags`
for SDK config overrides. The flag machinery — `FeatureFlagsRepository`, remote config types, bucketing —
stays internal to the SDK modules.

An override naming a key the SDK no longer knows is ignored rather than rejected, so a stale line in
a partner's test setup does not fail their build after they upgrade.

## Deliberately not built

- **No flag-change stream.** `isEnabled` is a synchronous read of a snapshot, and a refresh does not
  retroactively change answers already given. A flag flipping mid-session is a worse experience than
  one that changes on next launch, and an SDK that re-renders a partner's screen because a config
  poll returned is an SDK with a support ticket.
- **No analytics on flag exposure.** Real rollouts need it to compare cohorts; here it would be a
  network call to nowhere.
- **No TTL / expiry on the cached config.** Stale flags remain until the next successful fetch; that
  is the trade-off for offline cold starts. A production system would usually add max-age and still
  treat `enabled: false` as an immediate kill switch once fetched.
