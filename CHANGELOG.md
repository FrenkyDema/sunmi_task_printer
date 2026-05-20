## 0.1.0

**Major Architecture Refactor & Modernization**

* **Native Android Overhaul:**
    * Migrated the Android build system from legacy Groovy to Gradle Kotlin DSL (
      `build.gradle.kts`).
    * Bumped `compileSdk` to 36 and updated Java compatibility to Java 17.
    * Completely refactored `SunmiTaskPrinterMethod` to use non-blocking asynchronous calls.
      Hardware operations now run on a dedicated background executor, preventing Flutter UI thread
      freezes and ANR (Application Not Responding) crashes.
    * Resolved transaction buffer deadlocks by cleanly decoupling `MethodChannel.Result` resolution
      from physical hardware callbacks.
    * **New Feature:** Implemented an `EventChannel` (`hardwareErrorStream`) to broadcast real-time
      hardware mechanical failures (e.g., out of paper, overheating, cutter jams) directly to Dart.

* **Dart & Plugin Enhancements:**
    * Optimized `printRow` data parsing: Removed heavy JSON string serialization overhead in favor
      of passing native `List<Map<String, dynamic>>`.
    * Improved service binding and unbinding logic to properly queue, wait for, and resolve pending
      connection results.
    * Printer status codes are now accurately mapped to human-readable string values for easier
      debugging.
    * Hardened the codebase with strict typings to ensure maximum compatibility and zero warnings on
      `pub.dev` static analysis.

* **Example App & Tooling:**
    * Completely overhauled the Example App UI into clean, categorized testing cards for easier
      debugging on physical Sunmi POS terminals.
    * Introduced fully automated GitHub Actions workflows for CI/CD, including code analysis, test
      coverage tracking, and secure OIDC automated publishing to pub.dev.

## 0.0.3

* Refactoring for matching Dart formatter.

## 0.0.2

* Refactoring

## 0.0.1

* First release, with the implemented task manager