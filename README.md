# Duel LP Counter

A professional, lightweight Life Point counter for trading card games, featuring a grounded **Forbidden Memories** aesthetic.

## 🌟 Key Features

- **Retro Aesthetic**: Immersive ancient stone panel theme with metallic gold accents and classic serif typography.
- **Face-to-Face UI**: Two-player layout with the opponent's area rotated 180° for convenient table-top use.
- **Precision Life Point Control**:
  - Direct large-increment adjustments (±1000).
  - Multi-row quick buttons for ±500, ±100, and ±50.
  - Manual "Set LP" tool for specific values.
- **Master Duelist Utilities**:
  - **Ancient Stone Dice**: High-entropy D6 roll using `SecureRandom` with custom pip rendering.
  - **Minted Gold Coin**: Smooth 3D-effect flip animation with radial metallic gradients.
  - **Tactile Calculator**: Full-featured calculator with player selection to apply results directly to LP.
- **Advanced Duel State**:
  - LP change feedback animations (floating indicators).
  - Victory overlay with winner announcement and "New Duel" quick-reset.
  - Multi-choice Reset menu (8000, 4000, or Custom).

## ⚡ Performance & Privacy

- **Battery Optimized**: Zero background drain, zero polling, and lightweight animations that stop completely when not in use.
- **Privacy First**: No internet permission, no ads, and no analytics. Works 100% offline.
- **State Persistence**: Duel state (LP, names, tool results) is saved automatically using `rememberSaveable`.

## 🛠️ Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Minimum SDK**: API 26 (Android 8.0)
- **Architecture**: Single-Activity, State-Driven.

## 🚀 Build Instructions

1. Clone the repository: `git clone https://github.com/yourusername/DuelLPCounter.git`
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Gradle and run the `app` module on your device or emulator.

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
*Note: This app is a fan-made tool intended for personal use and is not affiliated with any official TCG franchises.*
