package io.github.fxzjshm.gdx.svg2pixmap.test;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap;

public class Svg2PixmapTest {

    public static int[] xs = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
    public static int[] ys = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

    @BeforeClass
    public static void init() throws IOException {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.allowSoftwareMode = true;
        config.forceExit = false;
        new LwjglApplication(new ApplicationListener() {
            @Override
            public void create() {
                Gdx.app.exit();
            }

            @Override
            public void resize(int width, int height) {

            }

            @Override
            public void render() {

            }

            @Override
            public void pause() {

            }

            @Override
            public void resume() {

            }

            @Override
            public void dispose() {

            }
        }, config);
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
    }

    public static double comparePixmaps(Pixmap p1, Pixmap p2) {
        if (p1.getWidth() == p2.getWidth() && p1.getHeight() == p2.getHeight()) {
            return LBG(p1, p2);
        } else throw new IllegalArgumentException("The two Pixmaps must be the same size!");
    }

    public static double comparePixmaps_samePixels(Pixmap p1, Pixmap p2) {
        long total = p1.getWidth() * p1.getHeight(), same = 0;
        for (int i = 0; i < p1.getWidth(); i++) {
            for (int j = 0; j < p1.getHeight(); j++) {
                if (p1.getPixel(i, j) == p2.getPixel(i, j)) same++;
            }
        }
        return same * 1.0 / total;
    }

    public static double[][] gray(Pixmap pixmap) {
        double[][] result = new double[pixmap.getWidth()][pixmap.getHeight()];
        Color c = new Color();
        for (int i = 0; i < pixmap.getWidth(); i++) {
            for (int j = 0; j < pixmap.getHeight(); j++) {
                Color.rgba8888ToColor(c, pixmap.getPixel(i, j));
                // Y = 0.299R+0.587G+0.114B
                result[i][j] = (0.299 * c.r + 0.587 * c.g + 0.114 * c.b - 1) * c.a + 1;
                // Gdx.app.debug("Color", "" + c);
            }
        }
        return result;
    }

    public static double LBG(Pixmap pixmap1, Pixmap pixmap2) {
        double[][] p1 = gray(pixmap1), p2 = gray(pixmap2);
        int total = (pixmap1.getWidth() - 2) * (pixmap1.getHeight() - 2) * 8, same = 0;
        for (int i = 1; i < pixmap1.getWidth() - 1; i++) {
            for (int j = 1; j < pixmap1.getHeight() - 1; j++) {
                for (int k = 0; k < 8; k++) {
                    if (p1[i + xs[k]][j + ys[k]] > p1[i][j] == p2[i + xs[k]][j + ys[k]] > p2[i][j])
                        same++;
                }
            }
        }
        return 1.0 * same / total;
    }

    @Test
    public void testPath2Pixmap() {
        Pixmap pixmap = Svg2Pixmap.path2Pixmap(32, 32, "M16 2 L30 16 16 30 16 16 2 30 2 2 16 16 Z", new Color(0.2f, 0.3f, 0.4f, 0.5f), new Color(0.7f, 0.8f, 0.9f, 1), 0.1, new Pixmap(320, 320, Pixmap.Format.RGB565));
        // PixmapIO.writePNG(Gdx.files.external("svgtest.png"), pixmap);
    }

    @Test
    public void testSvg2Pixmap() throws TranscoderException, IOException {
        FileHandle[] files = Gdx.files.internal("core/src/test/resources/bytesize-icons/dist/icons").list();
        for (FileHandle file : files) {
            double result = compare(file);
            String message = "File " + file.name() + ": " + result;
            if (result < 0.9)
                Gdx.app.error("TestSvg2Pixmap", message);
        }
    }

    public double compare(FileHandle file) throws TranscoderException, IOException {
        Pixmap pixmap1 = Svg2Pixmap.svg2Pixmap(file.readString());
        PNGTranscoder transcoder = new PNGTranscoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(byteArrayOutputStream);
        transcoder.transcode(new TranscoderInput(file.read()), transcoderOutput);
        byte[] content = byteArrayOutputStream.toByteArray();
        Pixmap pixmap2 = new Pixmap(content, 0, content.length);
        // PixmapIO.writePNG(new FileHandle("build/temp/" + file.nameWithoutExtension() + "-1.png"), pixmap1);
        // PixmapIO.writePNG(new FileHandle("build/temp/" + file.nameWithoutExtension() + "-2.png"), pixmap2);
        byteArrayOutputStream.close();
        return comparePixmaps(pixmap1, pixmap2);
    }
}