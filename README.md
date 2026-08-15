# NeatFreak（連潔癖）

NeatFreak is an Android share target that expands selected Meta short links and removes tracking parameters while preserving functional parameters on ordinary web pages.

## Supported links

- Threads share links: resolve to the first post URL without following any subsequent login/error redirect, then remove the full query and fragment.
- Facebook post share links: resolve redirects, then remove the full query and fragment.
- Facebook external links (`l.facebook.com/l.php`): resolve to the external HTTPS destination, then remove `utm_*` and `fbclid`.
- Instagram profiles, posts, and reels: remove the full query and fragment.
- LinkedIn post short links (`lnkd.in/p/...`): resolve to the LinkedIn post, then remove the full query and fragment. Direct LinkedIn post links are cleaned the same way.
- Spotify content links: remove the full query and fragment.
- Steam app links: remove the full query and fragment.
- YouTube short links (`youtu.be`): convert to a canonical `youtube.com/watch?v=...` URL, preserve the playback-time parameter (`t`), and remove other parameters.
- YouTube watch links (`youtube.com/watch`): preserve the video ID (`v`) and playback time (`t`), and remove other parameters.
- Amazon US and Japan product links: keep only the canonical `/dp/{product code}/` path and remove all parameters and fragments.
- Other web links: remove only `utm_*` and `fbclid`.

The app can be launched normally, selected from the Android Sharesheet, or invoked through Android's Process Text action.

## Build

The project uses Android SDK 37, target SDK 36, Android Gradle Plugin 9.1.1, and Gradle 9.3.1.

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
