# Donut Pay Highlighter

Client-side Fabric mod (Minecraft 1.21.4). When someone pays you more than $3M, that chat message is faded from black to orange.

## Build
1. Install JDK 21 and Gradle.
2. Run `gradle wrapper` once, then `./gradlew build`.
3. The jar is in `build/libs/`. Put it in your `mods` folder with Fabric API.

## Customize
Edit `DonutPayHighlighter.java`:
- `THRESHOLD` – minimum amount
- `PAY_PATTERN` – regex for the server's pay message
- `START_COLOR` / `END_COLOR` – gradient colors
