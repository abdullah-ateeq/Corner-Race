# Corner Race 🏁

**Corner Race** is a polished, local two-player Android strategy game built completely with modern **Android, Kotlin, and Jetpack Compose**. 

Two players compete on an 8×8 grid to race their 8 pieces from their home corner to the opposite corner. Strategy, tactical stepping, and chain jumping are key to victory!

---

## 🌟 Key Features

* **Local 2-Player Gameplay**: Pass-and-play local multiplayer mode with real-time turn tracking and interactive board controls.
* **Dynamic Jump Chains**: Perform single steps or chain multiple jumps over friendly or rival pieces in a single turn.
* **Custom Board Themes**: Toggle between **Classic** wood/cream tones and **Midnight** dark themes, plus full **Dark Mode / Light Mode / System Theme** support.
* **Synthesized Audio Engine**: Zero media asset bloat — features a lightweight, pure Kotlin/Android `AudioTrack` sound synthesizer generating custom SFX in real-time.
* **Haptic Feedback & Visual Cues**: Clear legal move indicators, target zone highlights, selected piece rings, and tactile feedback on actions.
* **Game Statistics & Settings Persistence**: Powered by Jetpack DataStore to persist preferences (sound, haptics, themes, move hints) and match stats (games played, win rates, fewest turns record).
* **Responsive Layouts**: Supports portrait and landscape orientations seamlessly for smartphones and tablets.
* **Undo & Turn Management**: Full move history support allowing move undo during turns.

---

## 🎮 How to Play

### **Objective**
Move all **8 pieces** from your starting corner into the **opposite corner** before your opponent does.

### **Rules**
1. **Starting Positions**:
   * **Player 1** starts in the bottom-left corner.
   * **Player 2** starts in the top-right corner.
2. **Move Types**:
   * **Step**: Move 1 space into any empty adjacent cell (up, down, sideways, or diagonal).
   * **Jump**: Jump over any adjacent piece (yours or your opponent's) into the empty cell directly beyond it.
   * **Multi-Jump Chains**: After a jump, you can continue jumping with the same piece if another valid jump exists, or tap **END TURN** to conclude your move.
3. **No Captures**: Pieces are never captured or removed from the board.
4. **Victory**: The first player to place all 8 pieces inside the opponent's starting corner wins the match!

---

## 🛠️ Tech Stack & Architecture

* **Language**: 100% [Kotlin](https://kotlinlang.org/)
* **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with **Material Design 3**
* **Graphics & Canvas**: Custom custom-drawn `Canvas` rendering for game pieces, movement hints, grid cells, and selection animations.
* **Architecture**: Modern Android Architecture with Unidirectional Data Flow (UDF), ViewModel, StateFlow, and a pure Kotlin domain game engine (`CornerRaceGame`, `BoardRules`).
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Data Storage**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
* **Sound Engine**: Synthesized PCM Audio with Android `AudioTrack` (`SoundEngine.kt`)
* **Testing**: JUnit 4 unit test suite verifying turn state transitions, legal step/jump move generation, and win condition validations.

---

## 📁 Project Structure

```
com.example.cornerrace/
├── data/              # DataStore Preferences & Settings Repository
├── feedback/          # Real-time Synthesized Audio & Haptics Engine
├── game/              # Pure Kotlin Game Engine (State, BoardRules, Move Validation)
├── ui/
│   ├── board/         # Compose Canvas Board Painter & GameBoard UI
│   ├── components/    # Reusable Material 3 UI Components & Buttons
│   ├── game/          # GameScreen, GameViewModel, and Dialogs
│   ├── home/          # HomeScreen
│   ├── info/          # HowToPlayScreen & AboutScreen
│   ├── result/        # Match Result / Winner Screen
│   ├── settings/      # SettingsScreen & Statistics View
│   ├── splash/        # SplashScreen
│   └── theme/         # Material 3 Color Schemes & Typography
└── MainActivity.kt    # Main Entry Point
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug (2024.2+) or newer
* **JDK**: Version 11 or higher
* **Min SDK**: API 24 (Android 7.0)
* **Target SDK**: API 35+ / API 37

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Corner-Race.git
   cd Corner-Race
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle and run the `:app` module on an emulator or Android device.

To execute unit tests:
```bash
./gradlew test
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for details.
