package io.github.fxzjshm.gdx.svg2pixmap.test.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import io.github.fxzjshm.gdx.svg2pixmap.test.TestCore;

/**
 * Launches the headless application. Can be converted into a utilities project or a server application.
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        TestCore testCore = new TestCore();
        createApplication(testCore);
        while (!testCore.finished)Thread.yield();
        if (testCore.fail) throw new Error("Failed. Use task ':run' for detail.");
    }

    private static Application createApplication(ApplicationListener applicationListener) {
        // Note: you can use a custom ApplicationListener implementation for the headless project instead of TestCore.
        return new Lwjgl3Application(applicationListener, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        return configuration;
    }
}