package io.github.fxzjshm.gdx.svg2pixmap.test;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class TestCore extends ApplicationAdapter {
    public static FileHandle[] svgFiles = new FileHandle[0];
    public static Pixmap[] results1 = new Pixmap[0], results2 = new Pixmap[0];

    public boolean fail = false, finished = false;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        try {
            testPath2Pixmap();
            testSvg2Pixmap();
            testStandardComparison();
        } catch (Throwable t) {
            t.printStackTrace();
            fail = true;
        } finally {
            finished = true;
        }
    }

    public static void testPath2Pixmap() {
        Pixmap pixmap = Svg2Pixmap.path2Pixmap(32, 32, "M16 2 L30 16 16 30 16 16 2 30 2 2 16 16 Z", new Color(0.2f, 0.3f, 0.4f, 0.5f), new Color(0.7f, 0.8f, 0.9f, 1), 0.1, new Pixmap(320, 320, Pixmap.Format.RGB565));
        // PixmapIO.writePNG(Gdx.files.external("svgtest.png"), pixmap);
    }

    public static void testSvg2Pixmap() {
        FileHandle directory = Gdx.files.internal("bytesize-icons/dist/icons");
        String[] prefixes = {"", "assets/", "../assets/", "test-libgdx/assets/"};
        for (int i = 0; i < prefixes.length && svgFiles.length == 0; i++) {
            svgFiles = Gdx.files.internal(prefixes[i] + directory.path()).list();
        }
        if (svgFiles.length == 0) {
            throw new RuntimeException("No test file found in " + directory.path());
        }
        results1 = new Pixmap[svgFiles.length];
        for (int i = 0; i < svgFiles.length; i++) {
            FileHandle file = svgFiles[i];
            results1[i] = Svg2Pixmap.svg2Pixmap(file.readString());
        }
    }

    public void testStandardComparison() throws ReflectionException {
        try {
            ClassReflection.getMethod(ClassReflection.forName("io.github.fxzjshm.gdx.svg2pixmap.test.Comparison"), "compareToStandardResults")
                    .invoke(null);
        } catch (ReflectionException exception) {
            // this class does not exist in GWT mode, no need to care about exceptions
            if (!Gdx.app.getType().equals(Application.ApplicationType.WebGL)) {
                throw exception;
            }
        }
    }
}