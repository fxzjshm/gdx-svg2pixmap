package io.github.fxzjshm.gdx.svg2pixmap.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.async.ThreadUtils;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap;

import static io.github.fxzjshm.gdx.svg2pixmap.test.TestCore.*;

/**
 * Compare to the standard converters. Currently choosing Apache Batik
 */
@SuppressWarnings("unused")
public class Comparison {

    @SuppressWarnings("unused")
    public static void compareToStandardResults() throws TranscoderException, IOException {
        boolean fail = false;
        if (svgFiles.length == 0) {
            testSvg2Pixmap();
        }
        results2 = new Pixmap[svgFiles.length];

        AtomicInteger count = new AtomicInteger(0);
        for (int i = 0; i < svgFiles.length; i++) {
            int j = i;
            asyncExecutor.submit(() -> {
                TestCore.results2[j] = standardConvert(svgFiles[j]);
                count.incrementAndGet();
                return null;
            });
        }
        while (count.get() < svgFiles.length) {
            ThreadUtils.yield();
        }

        for (int i = 0; i < svgFiles.length; i++) {
            FileHandle file = svgFiles[i];
            Pixmap pixmap1 = results1[i];
            Pixmap pixmap2 = results2[i];
            double result = comparePixmaps(pixmap1, pixmap2);
            String message = "File " + file.name() + ": " + result;
            Gdx.app.log("TestSvg2Pixmap", message);
            if (result < 0.9) {
                Gdx.app.error("compareToStandardResults", "Compare failed: " + file.name());
                fail = true;
            }
        }
        if (fail) {
            throw new GdxRuntimeException("Compare failed.");
        }
    }

    public static Pixmap standardConvert(FileHandle file) throws TranscoderException, IOException {
        PNGTranscoder transcoder = new PNGTranscoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        TranscoderOutput transcoderOutput = new TranscoderOutput(byteArrayOutputStream);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) (width * outputScale));
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) (height * outputScale));
        transcoder.transcode(new TranscoderInput(file.read()), transcoderOutput);
        byte[] content = byteArrayOutputStream.toByteArray();
        Pixmap pixmap2 = new Pixmap(content, 0, content.length);
        byteArrayOutputStream.close();
        return pixmap2;
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


    public static int[] xs = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};
    public static int[] ys = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};

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
}
