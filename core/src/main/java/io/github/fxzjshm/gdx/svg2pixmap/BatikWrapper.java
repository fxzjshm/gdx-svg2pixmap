package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.graphics.Pixmap;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class BatikWrapper {
    public static Pixmap svg2Pixmap(String fileContent) throws TranscoderException {
        PNGTranscoder pngTranscoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(fileContent.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);
        pngTranscoder.transcode(input, output);
        byte[] imageData = outputStream.toByteArray();
        return new Pixmap(imageData, 0, imageData.length);
    }
}
