# Changelog
All notable changes to this project will be documented in this file.

## [2.0.2]

### Changes
- Add missing lifecycle check in `MainThreadJob.doNow()`

## [2.0.1]

### Changes
- Changed interface of worker from `() -> Unit` to `Job.() -> Unit` which makes it easier to run tasks periodically
*Please note* for Java implementations this is a breaking change

## [2.0.0]

### Changes
- Rewrite in Kotlin
- Support for Android lifecycle

## [1.0.0]
- First published