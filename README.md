# SmartAlarm

A smart alarm application project.

## Getting Started

This project is currently in development.

## Current Capabilities

- Landscape-optimised bedside clock with auto-dim and burn-in mitigation
- Exact alarm scheduling with snooze support and SherpaTTS playback fallback to Android TTS
- To-do list with Room persistence and spoken playback
- Navigation shell for alarm setup, clock, to-do, and settings

## Building the App

```bash
# Ensure an emulator or device is available if you plan to run instrumentation tests
./gradlew assembleDebug
```

## Emulator & Instrumentation Testing

1. Launch an Android 13/14 emulator (or connect a device with USB debugging enabled).
2. From the project root run:

   ```bash
   ./gradlew connectedDebugAndroidTest
   ```

   This executes `NavigationSmokeTest`, validating the primary navigation flow on device/emulator.

## Settings & Tuning

The Settings screen lets you adjust the screen auto-dim timeout and choose the preferred TTS engine (Sherpa-only, Android-only, or automatic).

## Next Steps

- Expand UI polish and multi-alarm management
- Add additional automated test coverage and device-specific tuning

## Features

- Coming soon...

## Installation

Instructions will be added as the project develops.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.
