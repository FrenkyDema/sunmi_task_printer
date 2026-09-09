## 0.2.0

**Cash drawer reliability & service binding fixes**

* **Breaking:** `openDrawer()` now completes only once the printer firmware confirms the kick.
  It previously resolved as soon as the AIDL call was dispatched, so a drawer that never opened
  still reported success. It now throws a `PlatformException` with code `OPERATION_FAILED`,
  `PRINTER_EXCEPTION`, `UNAVAILABLE` or `TIMEOUT`.
* **Breaking:** `drawerStatus()` and `drawerTimesOpen()` no longer return `false` / `0` when the
  printer service is unreachable — they throw a `PlatformException` with code `UNAVAILABLE`.
  A dead binding used to be indistinguishable from a drawer that is simply not attached.
* **Fixed:** Added the `<queries>` declaration for `woyou.aidlservice.jiuiv5` to the plugin
  manifest. On Android 11+ with `targetSdk >= 30`, package visibility filtering made the Sunmi
  service invisible, so `bindService()` returned `false` and the printer was reported as
  "not found" even when installed. Host apps no longer need to declare this themselves.
* **Fixed:** `BIND_SERVICE` could never resolve. The pending `Result` was only completed from
  `onServiceConnected`, so a refused or failed bind left the Dart future awaiting forever — which
  froze app startup for any host that awaited it before `runApp()`. Every exit path now settles
  the result, including a `false` return from `bindService()`, `onNullBinding` and `onBindingDied`.
* **Fixed:** The connection is now re-established after `onBindingDied`, and commands that find the
  service missing schedule a reconnect. Previously a service restart left the plugin permanently
  broken until the host app was restarted.
* **Fixed:** `unbindService()` now releases a connection that was requested but never completed,
  and the plugin cleans up when the Flutter engine detaches.
* In-flight AIDL callbacks are held with a strong reference so they cannot be garbage collected
  before the printer replies, and are bounded by a 5s timeout.

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