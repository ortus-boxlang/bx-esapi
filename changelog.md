# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

* * *

## [Unreleased]

## [1.9.0] - 2026-05-13

### Improvements

- Bump com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer from 20260102.1 to 20260313.1
- Added struct policy caching and eviction capabilities for `getSafeHTML()` and `isSafeHTML()`
- Added `force` boolean argument to `getSafeHTML()` and `isSafeHTML()` for struct policies to evict cached compiled policy entries and rebuild on demand

### Fixed

- Fixed changelog so we could publish github releases.

## [1.8.0] - 2026-05-12

### Added

- `getSafeHTML()` and `isSafeHTML()` now accept a struct for programmatic policy configuration in addition to a string policy name
- Struct policies support overriding a built-in base policy (merge or override mode) or building an entire policy from scratch
- Supported struct keys: `basePolicy`, `overrideMode`, `directives`, `allowTags`, `tagRules`, `globalAttributes`, `dynamicAttributes`, `cssRules`, `allowedEmptyTags`, `requireClosingTags`, `tagsToEncode`
- Added `throwOnError` boolean argument to `getSafeHTML()` (default `false`) — when `true`, throws an exception if HTML violates policy rules instead of silently returning sanitized output

### Changed

- Replaced reflection-based AntiSamy policy override approach with clean XML DOM generation

## [1.7.0] - 2026-01-09

## [1.6.0] - 2025-07-02

## [1.5.0] - 2025-06-09

### Added

- Bumps org.owasp.esapi:esapi from 2.6.2.0 to 2.7.0.0.
- Enabled `encodeForSQL()` due to being off by default in 2.7

## [1.4.0] - 2025-05-26

### Added

- Bump org.owasp.esapi:esapi from 2.6.0.0 to 2.6.1.0
  - <https://github.com/ESAPI/esapi-java-legacy/blob/develop/documentation/esapi4java-core-2.6.1.0-release-notes.txt>

### Fixed

- Fixed bump versions in the `gradle.build` file

## [1.3.1] - 2025-04-09

### Fixed

- Added missing `Logger.LogPrefix=false` to `bx-esapi.properties` file

## [1.3.0] - 2025-04-04

### Added

- Missing member methods several Box Modules use
- New Esapi Service to take care of service loading or instantiation capabilities
- Muted initialization of ESAPI logger
- Real integration testing
- Updated to latest ESAPI

## [1.2.0] - 2025-02-22

### Fixed

- `sanitizeHTML()` policy argument can be a string or a real policy object
- JUnit Runtime Runner for tests
- Preferred way to exclude test resources

### Added

- Build updates
- Lots of docs updates

## [1.1.0] - 2024-09-16

### Added

- Upgraded to all latest CI
- Upgraded to all latest CI

## [1.0.0] - 2024-06-13

- Upgraded to latest ESAPI due to CVE
- First iteration of this module

[unreleased]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.9.0...HEAD
[1.9.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.6.0...v1.7.0
[1.6.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/ortus-boxlang/bx-esapi/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/ortus-boxlang/bx-esapi/compare/251f3772e721f1f7aea3f7d2e2da602b8af97a40...v1.0.0
