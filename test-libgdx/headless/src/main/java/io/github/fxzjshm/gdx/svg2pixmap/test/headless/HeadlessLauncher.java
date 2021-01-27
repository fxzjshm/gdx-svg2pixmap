package io.github.fxzjshm.gdx.svg2pixmap.test.headless;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import io.github.fxzjshm.gdx.svg2pixmap.test.TestCore;

/**
 * Launches the headless application. Can be converted into a utilities project or a server application.
 */
public class HeadlessLauncher {
    public static void main(String[] args) {
        TestCore testCore = new TestCore();
        createApplication(testCore);
        while (!testCore.finished) Thread.yield();
        if (testCore.fail)
            throw new Error("Failed. Use task ':run' for detail.", testCore.throwable);
    }

    private static Application createApplication(ApplicationListener applicationListener) {
        // Note: you can use a custom ApplicationListener implementation for the headless project instead of TestCore.
        return new HeadlessApplication(applicationListener, getDefaultConfiguration());
    }

    private static HeadlessApplicationConfiguration getDefaultConfiguration() {
        HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        configuration.renderInterval = -1f; // When this value is negative, TestCore#render() is never called.
        return configuration;
    }
}