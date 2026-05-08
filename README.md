# yt-dlp Downloader (Android)

A minimal Android app that wraps [yt-dlp](https://github.com/yt-dlp/yt-dlp) using [Chaquopy](https://chaquo.com/chaquopy/) to download videos from yt-dlp-supported sites.

## Stack

- Kotlin + Jetpack Compose (Material 3)
- Chaquopy 15.0.1 (Python 3.11 inside the APK)
- yt-dlp (latest, installed via pip at build time)
- Min SDK 26 / Target SDK 34
- Android Gradle Plugin 8.2.2

## Important caveats

**Google Play Store will likely reject this app.** Google's Developer Program Policies forbid apps that download content from services whose terms of service prohibit it. Reference apps like Seal, NewPipe, and YTDLnis ship via F-Droid or direct APK only. Plan for that distribution path.

**No ffmpeg bundled.** This keeps the APK small (~80 MB instead of ~150 MB) but limits YouTube quality to ~720p (the highest single-format mp4 stream). To unlock 1080p+ merging, add ffmpeg-kit and update `format` in `ytdlp_wrapper.py`.

**yt-dlp updates frequently.** YouTube and others routinely break extraction. Each new APK build pulls the latest yt-dlp, but if you ship a stable APK, users will eventually need an update. A real production app should fetch yt-dlp wheel updates at runtime.

## Building locally

Prerequisites:
- JDK 17
- Android SDK (API 34) and Build Tools
- Either Android Studio Hedgehog+ OR Gradle 8.5+ on the command line

First-time setup (only needed if `gradlew` is not present):

```bash
gradle wrapper --gradle-version 8.5 --distribution-type bin
```

Build a debug APK:

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

The first build takes 5 to 10 minutes because Chaquopy downloads Python and yt-dlp wheels. Subsequent builds are much faster.

## Building via GitHub Actions

The workflow at `.github/workflows/build.yml` builds the APK on every push to `main`/`master` and on manual trigger.

To build:
1. Push the repo to GitHub.
2. Go to the **Actions** tab and either wait for the auto-trigger or click **Run workflow** on the *Build APK* workflow.
3. When it finishes, download the artifact `yt-downloader-debug-apk` from the run page.

Tagging a commit with `v*` (e.g. `git tag v1.0 && git push origin v1.0`) attaches the APK to a GitHub Release automatically.

## Installing the APK

1. Transfer the APK to your phone (email, USB, etc).
2. On the phone, allow installation from unknown sources for your file manager or browser.
3. Open the APK and install.
4. Open the app, paste a URL, tap **Download**.

Files save to `Android/data/com.ytdlp.downloader/files/Downloads/` on the phone's internal storage. Use the **Share** button in the app or a file manager to move them elsewhere.

## Release signing (when you're ready)

The Actions workflow currently builds an unsigned debug APK. For release builds:

1. Generate a keystore locally:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias yt-downloader \
     -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Base64-encode it and store as repo secret `KEYSTORE_BASE64`:
   ```bash
   base64 -w 0 release.keystore | xclip -selection clipboard
   ```
3. Add secrets `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
4. Update `app/build.gradle.kts` with a `signingConfigs.release` block reading those values from env vars, and switch the workflow to `assembleRelease`.

I left this out of v1 to keep the workflow runnable out of the box — ask me to wire it up when you have a keystore.

## Project layout

```
.
├── .github/workflows/build.yml      GitHub Actions CI
├── app/
│   ├── build.gradle.kts             Module config + Chaquopy + Compose
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ytdlp/downloader/
│       │   ├── YtDlpApplication.kt  Boots Python on launch
│       │   ├── MainActivity.kt      Entry point + share intent handler
│       │   ├── MainViewModel.kt     UI state + coroutines
│       │   └── ui/
│       │       ├── DownloaderScreen.kt
│       │       └── theme/
│       ├── python/
│       │   └── ytdlp_wrapper.py     Wraps yt-dlp.YoutubeDL
│       └── res/
├── build.gradle.kts                 Root build config
├── settings.gradle.kts              Repos + Chaquopy maven
└── gradle.properties
```

## Things to add next

- Format picker (audio-only mp3, choose resolution)
- Download progress callback from Python to Kotlin
- Foreground service so downloads survive backgrounding
- Bundle ffmpeg-kit for high-res merging
- In-app yt-dlp self-update
- Save destination via Storage Access Framework
- Download history list

## License

Your code: choose one (MIT/Apache-2.0 typical).
yt-dlp is Unlicense. Chaquopy has its own license — review at https://chaquo.com/chaquopy/license/ before publishing commercially.
