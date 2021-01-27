package io.github.fxzjshm.gdx.svg2pixmap.test;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.ThreadUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.concurrent.atomic.AtomicInteger;

import io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class TestCore extends ApplicationAdapter {
    public static FileHandle[] svgFiles = new FileHandle[0];
    public static Pixmap[] results1 = new Pixmap[0], results2 = new Pixmap[0];
    public static Pixmap none;

    public static int width = 32, height = 32;
    public static double drawScale = 10, outputScale = 2;

    public static AsyncExecutor asyncExecutor = new AsyncExecutor(Runtime.getRuntime().availableProcessors());

    public Pixmap pixmap1, pixmap2;
    public int index = -1;

    public SpriteBatch batch;
    public TextureRegion textureRegion1, textureRegion2;
    public Image image1, image2;
    public Camera camera;
    public Viewport viewport;
    public Stage stage;

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

        if (Gdx.app.getType() != Application.ApplicationType.HeadlessDesktop) {
            createUI();
        }
    }

    public void createUI() {
        // none = new Pixmap(1, 1, Pixmap.Format.Alpha);
        none = new Pixmap(Gdx.files.internal("libgdx.png"));
        pixmap1 = pixmap2 = none;

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        if (Gdx.input.getInputProcessor() != null) {
            inputMultiplexer.addProcessor(Gdx.input.getInputProcessor());
        }
        inputMultiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                nextPixmap();
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                nextPixmap();
                return true;
            }
        });
        Gdx.input.setInputProcessor(inputMultiplexer);

        batch = new SpriteBatch();
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new ScalingViewport(Scaling.stretch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        stage = new Stage(viewport, batch);

        textureRegion1 = new TextureRegion(new Texture(pixmap1));
        // textureRegion1.setRegion(0, 0, (int) (width * scale), (int) (height * scale));
        image1 = new Image(textureRegion1);
        image1.setBounds(0, 0, (int) (width * drawScale), (int) (height * drawScale));
        stage.addActor(image1);

        textureRegion2 = new TextureRegion(new Texture(pixmap2));
        // textureRegion2.setRegion(0, 0, (int) (width * scale), (int) (height * scale));
        image2 = new Image(textureRegion2);
        image2.setBounds(Gdx.graphics.getWidth() - (int) (width * drawScale), 0, (int) (width * drawScale), (int) (height * drawScale));
        stage.addActor(image2);

        stage.setDebugAll(true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();
        stage.draw();
    }

    public void nextPixmap() {
        index++;
        pixmap1 = ((0 <= index && index < results1.length && results1[index] != null) ? results1[index] : none);
        pixmap2 = ((0 <= index && index < results2.length && results2[index] != null) ? results2[index] : none);

        textureRegion1.getTexture().dispose();
        textureRegion1 = new TextureRegion(new Texture(pixmap1));
        ((TextureRegionDrawable) (image1.getDrawable())).setRegion(textureRegion1);

        textureRegion2.getTexture().dispose();
        textureRegion2 = new TextureRegion(new Texture(pixmap2));
        ((TextureRegionDrawable) (image2.getDrawable())).setRegion(textureRegion2);

        Gdx.app.debug("nextPixmap", "index = " + index);
    }

    public static void testPath2Pixmap() {
        long time = TimeUtils.millis();
        Pixmap pixmap = Svg2Pixmap.path2Pixmap(32, 32, "M16 2 L30 16 16 30 16 16 2 30 2 2 16 16 Z", new Color(0.2f, 0.3f, 0.4f, 0.5f), new Color(0.7f, 0.8f, 0.9f, 1), 0.1, new Pixmap(320, 320, Pixmap.Format.RGB565));
        Gdx.app.debug("testPath2Pixmap", TimeUtils.millis() - time + "ms");
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
        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < svgFiles.length; i++) {
            int j = i;

            if (Gdx.app.getType().equals(Application.ApplicationType.WebGL)) {
                long time = TimeUtils.millis();
                Svg2Pixmap.svg2PixmapJSNI(svgFiles[j].readString(), (int) (width * outputScale), (int) (height * outputScale), pixmap -> {
                    results1[j] = pixmap;
                    Gdx.app.debug("testSvg2Pixmap", svgFiles[j].name() + " " + (TimeUtils.millis() - time) + "ms");
                    count.incrementAndGet();
                });
            } else {
                asyncExecutor.submit(() -> {
                    long time = TimeUtils.millis();
                    results1[j] = Svg2Pixmap.svg2Pixmap(svgFiles[j].readString(), (int) (width * outputScale), (int) (height * outputScale));
                    Gdx.app.debug("testSvg2Pixmap", svgFiles[j].name() + " " + (TimeUtils.millis() - time) + "ms");
                    count.incrementAndGet();
                    return null;
                });
            }

        }
        if (!Gdx.app.getType().equals(Application.ApplicationType.WebGL)) {
            while (count.get() < svgFiles.length) {
                ThreadUtils.yield();
            }
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