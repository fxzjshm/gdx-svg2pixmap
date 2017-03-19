package io.github.fxzjshm.gdx.svg2pixmap.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

    @BeforeClass
    public static void init() throws IOException {
        LwjglNativesLoader.load();
        Gdx.files = new LwjglFiles();
    }

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.allowSoftwareMode = true;
        ApplicationAdapter emptyApplication = new ApplicationAdapter() {
            Texture t;
            SpriteBatch sb;

            @Override
            public void create() {
                super.create();
                sb = new SpriteBatch();
                t = new Texture(Svg2Pixmap.path2Pixmap(32, 32, "M16 2 L30 16 16 30 16 16 2 30 2 2 16 16 Z", new Color(0.2f, 0.3f, 0.4f, 0.5f), new Color(0.7f, 0.8f, 0.9f, 1), 5, new Pixmap(320, 240, Pixmap.Format.RGB565)));
            }

            @Override
            public void render() {
                super.render();
                sb.begin();
                sb.draw(t, 0, 0);
                sb.end();
            }
        };
        new LwjglApplication(emptyApplication, config);
    }

    public static double comparePixmaps(Pixmap p1, Pixmap p2) {
        if (p1.getWidth() == p2.getWidth() && p1.getHeight() == p2.getHeight()) {
            long total = p1.getWidth() * p1.getHeight(), same = 0;
            for (int i = 0; i < p1.getWidth(); i++) {
                for (int j = 0; j < p1.getHeight(); j++) {
                    if (p1.getPixel(i, j) == p2.getPixel(i, j)) same++;
                }
            }
            return same / total;
        } else throw new IllegalArgumentException("The two Pixmaps must be the same size!");
    }

    @Test
    public void testPath2Pixmap() {
        Pixmap pixmap = Svg2Pixmap.path2Pixmap(32, 32, "M16 2 L30 16 16 30 16 16 2 30 2 2 16 16 Z", new Color(0.2f, 0.3f, 0.4f, 0.5f), new Color(0.7f, 0.8f, 0.9f, 1), 0.1, new Pixmap(320, 320, Pixmap.Format.RGB565));
        // PixmapIO.writePNG(Gdx.files.external("svgtest.png"), pixmap);
    }

    @Test
    public void svg2Pixmap() throws TranscoderException {
        FileHandle[] files = Gdx.files.internal("core/src/test/resources/bytesize-icons/dist/icons").list();
        for (FileHandle file : files) {
            Pixmap pixmap1 = Svg2Pixmap.svg2Pixmap(file.readString());
            PNGTranscoder transcoder = new PNGTranscoder();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            TranscoderOutput transcoderOutput = new TranscoderOutput(byteArrayOutputStream);
            transcoder.transcode(new TranscoderInput(file.read()), transcoderOutput);
            byte[] content = byteArrayOutputStream.toByteArray();
            Pixmap pixmap2 = new Pixmap(content, 0, content.length);
            System.out.println(comparePixmaps(pixmap1,pixmap2));
        }
    }
}