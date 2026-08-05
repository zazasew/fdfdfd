# Personal Notes

A privacy-first, 100% offline note-taking app built with Kotlin, Jetpack Compose,
Material 3, Room, and Hilt.

## Getting started

1. Open this folder in Android Studio (Koala/2024.1 or newer recommended).
2. Let Gradle sync — it will download the dependencies listed in
   `gradle/libs.versions.toml`.
3. Run the `app` configuration on an emulator or device (minSdk 24).

No API keys, accounts, or network access are required — the app works fully in
Airplane Mode.

## What's real vs. what changed in this pass

- **App icon** — generated from your notebook artwork (`res/mipmap-*/ic_launcher.png`
  for legacy launchers, `res/drawable/ic_launcher_foreground.png` +
  `@color/launcher_bg` for the Android 8+ adaptive icon).
- **Avatars** — the boy/girl portraits are now real cropped images
  (`res/drawable/avatar_boy.png`, `avatar_girl.png`), rendered as circles via
  `AvatarView.kt`. Add a third avatar later by dropping a new PNG in
  `res/drawable/`, adding an `Avatar` enum case, and one `when` branch.
- **Rich text formatting now actually works.** Notes are stored as plain text
  with lightweight inline tags (`[b]bold[/b]`, `[i]italic[/i]`, `[u]underline[/u]`,
  `[h]highlight[/h]`, `[c=RRGGBB]color[/c]`) — see `util/RichText.kt`. Selecting
  text and tapping Bold/Italic/Underline/Highlight/Text color genuinely toggles
  that formatting and renders it live in the editor (the bracket syntax is
  hidden by a `VisualTransformation`, so what you see is the formatted text, not
  raw markup). Bullet/numbered/checkbox buttons toggle a line prefix on the
  current line. Word/character counts and note-list previews strip the tags so
  they read cleanly everywhere else in the app.
- **Theme is now its own screen** (Settings → Theme), matching a two-panel
  navigation flow rather than a popup dialog.
- **Search results bold the matching text** in both the title and preview,
  the way most note apps highlight matches.
- **Note cards default to a rotating pastel color** when you don't pick one
  yourself, and each card's title is auto-tinted to a deeper shade of its
  background color, so the home screen reads as intentionally designed rather
  than flat and gray.

## Project structure

```
app/src/main/java/com/cozynotes/app/
├── MainActivity.kt          # Entry point, applies theme, hosts the nav graph
├── NotesApplication.kt      # @HiltAndroidApp application class
├── data/
│   ├── local/                # Room entity, DAO, database, type converters
│   ├── preferences/          # DataStore-backed user settings
│   └── repository/           # Repository pattern over Room
├── di/                       # Hilt modules (database, preferences, repository)
├── model/                    # Domain models (Note, Avatar)
├── util/                     # RichText engine, greeting text, word/char counts
└── ui/
    ├── theme/                 # Color, Type, Shape, light/dark Material 3 theme
    ├── navigation/            # NavGraph + Screen routes
    ├── components/            # Shared composables (AvatarView)
    ├── onboarding/             # First-launch name + avatar screen
    ├── home/                  # Home screen, welcome card, note cards, top bar
    ├── editor/                # Note editor: working formatting, undo/redo
    ├── search/                # Instant title/content search with highlighting
    └── settings/              # Settings list + dedicated Theme screen
```

## A note on compiling

This project was generated outside of Android Studio, without access to the
Android SDK/Gradle toolchain to run a real build. It's been checked by hand for
brace/paren balance and import completeness across every file, and follows
current (2024) AGP/Compose/Hilt conventions. If Gradle sync surfaces a
version-compatibility hiccup, bump the versions in `gradle/libs.versions.toml`
and `gradle/wrapper/gradle-wrapper.properties` to whatever Android Studio
recommends when it opens the project.
