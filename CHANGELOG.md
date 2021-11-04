# Changelog
All notable changes to this project will be documented in this file.

## 2.1.0

**Changes**

- Add attentional check if a task was canceled while it was put in the queue to be executed on the main thread
- Add support to add other work on the same lazy worker. You can use this to show results delayed

## 2.0.3

**Changes**

- Add new `ThreadingHelper` for mocking `Handler`s and `isOnMainThread` for testing code using this lib

## 2.0.2

**Changes**

- Add missing lifecycle check in `MainThreadJob.doNow()`

## 2.0.1

**Changes**

- Changed interface of worker from `() -> Unit` to `Job.() -> Unit` which makes it easier to run tasks periodically
**Please note** for Java implementations this is a breaking change

## 2.0.0

**Changes**

- Rewrite in Kotlin
- Support for Android lifecycle

## 1.0.0
- First published