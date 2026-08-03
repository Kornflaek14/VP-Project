package com.cardgame.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.cardgame.CardBattlerGame;

/**
 * Desktop entry point.
 *
 * <p>Run via Gradle:
 * <pre>
 * ./gradlew :lwjgl3:run
 * </pre>
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // required on macOS for LWJGL3

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Card Battler — 1v1 Duelling");
        config.setWindowedMode(1280, 720);
        // config.setWindowIcon("icon128.png", "icon64.png", "icon32.png", "icon16.png");
        config.setForegroundFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
        config.useVsync(true);

        new Lwjgl3Application(new CardBattlerGame(), config);
    }
}
