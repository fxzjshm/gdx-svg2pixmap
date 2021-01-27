package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.async.ThreadUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class Svg2Pixmap {
    /**
     * Generate shapes on a (width * generateScale) x (height * generateScale) Pixmap, then resize to the original size.
     * This seems to be called super-sampling.
     * TODO use "signed distance field" method
     * This affects {@link Svg2Pixmap#svg2Pixmap} but not {@link Svg2Pixmap#path2Pixmap}.
     */
    public static int generateScale = 2;

    public static Color defaultColor = Color.BLACK;

    /**
     * Convert a SVG {@code <path />} element into a {@link Pixmap}.
     * Will scale if (width != {@link Pixmap#getWidth()} || height != {@link Pixmap#getHeight()}).
     *
     * @param width       the origin width of the SVG file, maybe defined by viewbox
     * @param height      the origin height of the SVG file, maybe defined by viewbox
     * @param d           the property d of the origin path element
     * @param fill        the color to fill in the shape.
     * @param stroke      the color of th path
     * @param strokeWidth width to draw. WARNING: WILL BE SCALED if (width != {@link Pixmap#getWidth()} || height != {@link Pixmap#getHeight()}).
     * @param pixmap      the pixmap to draw the path in.
     * @return Drawn pixmap.
     */
    public static Pixmap path2Pixmap(int width, int height, String d, Color fill, Color stroke, double strokeWidth, Pixmap pixmap) {
        checkGWT();

        StringTokenizer stringTokenizer = new StringTokenizer(H.splitMixedTokens(d));
        int strokeRadius = (int) Math.round(strokeWidth * Math.sqrt(1.0 * (pixmap.getWidth() * pixmap.getHeight()) / (width * height)) / 2);

        Vector2 currentPosition = new Vector2(0, 0);// Current position. Used by commands.
        Vector2 initialPoint = new Vector2(0, 0);// Current position. Used by command 'M'.
        Vector2 lastCPoint = null; // Last control point of last 'C' or 'S' command.
        Vector2 lastQPoint = null; // Last control point of last 'Q' or 'T' command.
        boolean[][] border = new boolean[pixmap.getWidth()][pixmap.getHeight()]; // int[][] to save border position, assuming initial value are all false

        char lastCommand = 0; // Last command.
        LinkedList<String> params = new LinkedList<String>(); // Real parameters.
        while (stringTokenizer.hasMoreTokens()) {
            String tmp; // Next token. Maybe a command or an argument.
            char command; //  The real command.
            int paramAmount = 0; // The amount of parameters to read.

            try {
                tmp = stringTokenizer.nextToken();
            } catch (NoSuchElementException nsee) { // No more tokens.
                break;
            }

            if (1 == tmp.length() && Character.isLetter(tmp.charAt(0))/* That is, tmp is a command.*/) {
                lastCommand = command = tmp.charAt(0);
            } else {
                // tmp is not a command.
                if (lastCommand != 0) {
                    command = lastCommand;
                    // tmp is a parameter of last command.
                    params.add(tmp);
                    paramAmount--; //Have loaded one.
                } else throw new IllegalArgumentException("No command at the beginning ?");
            }
            paramAmount += H.getParamAmount(command);
            for (int i = 0; i < paramAmount; i++) {
                params.add(stringTokenizer.nextToken());
            }

            // convert relative positions to absolute positions
            H.r2a(command, params, new Vector2(currentPosition.x / pixmap.getWidth() * width, currentPosition.y / pixmap.getHeight() * height));

            char newCommand = Character.toUpperCase(command);
            if (newCommand == 'M') {
                initialPoint.x = currentPosition.x = (Float.parseFloat(params.get(0))) / width * pixmap.getWidth();
                initialPoint.y = currentPosition.y = (Float.parseFloat(params.get(1))) / height * pixmap.getHeight();
            }
            if (newCommand == 'Z') {
                H.drawCurve(pixmap, new Vector2[]{currentPosition, initialPoint}, stroke, strokeRadius, border);
            }
            if (newCommand == 'L') {
                float x2 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y2 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                H.drawCurve(pixmap, new Vector2[]{currentPosition, new Vector2(x2, y2)}, stroke, strokeRadius, border);

                currentPosition.x = x2;
                currentPosition.y = y2;
            }
            if (newCommand == 'H') {
                float x2 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth();
                H.drawCurve(pixmap, new Vector2[]{currentPosition, new Vector2(x2, currentPosition.y)}, stroke, strokeRadius, border);

                currentPosition.x = x2;
            }
            if (newCommand == 'V') {
                float y2 = Float.parseFloat(params.get(0)) / height * pixmap.getHeight();
                H.drawCurve(pixmap, new Vector2[]{currentPosition, new Vector2(currentPosition.x, y2)}, stroke, strokeRadius, border);

                currentPosition.y = y2;
            }
            if (newCommand == 'C') {
                float x1 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y1 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x2 = Float.parseFloat(params.get(2)) / width * pixmap.getWidth(), y2 = Float.parseFloat(params.get(3)) / height * pixmap.getHeight();
                float x = Float.parseFloat(params.get(4)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(5)) / height * pixmap.getHeight();
                lastCPoint = new Vector2(x2, y2);
                H.drawCurve(pixmap, new Vector2[]{currentPosition, new Vector2(x1, y1), lastCPoint, new Vector2(x, y)}, stroke, strokeRadius, border);

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'S') {
                float x2 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y2 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x = Float.parseFloat(params.get(2)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(3)) / height * pixmap.getHeight();
                float x1, y1;
                if (lastCPoint != null) {
                    x1 = 2 * currentPosition.x - lastCPoint.x;
                    y1 = 2 * currentPosition.y - lastCPoint.y;
                } else {
                    x1 = x2;
                    y1 = y2;
                }
                lastCPoint = new Vector2(x2, y2);
                H.drawCurve(pixmap, new Vector2[]{currentPosition, new Vector2(x1, y1), lastCPoint, new Vector2(x, y)}, stroke, strokeRadius, border);

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'Q') {
                float x1 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y1 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x = Float.parseFloat(params.get(2)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(3)) / height * pixmap.getHeight();
                lastQPoint = new Vector2(x1, y1);
                H.drawCurve(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2(x, y)}, stroke, strokeRadius, border);

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'T') {
                float x = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x1, y1;
                if (lastQPoint != null) {
                    x1 = 2 * currentPosition.x - lastQPoint.x;
                    y1 = 2 * currentPosition.y - lastQPoint.y;
                } else {
                    x1 = x;
                    y1 = y;
                }
                lastQPoint = new Vector2(x1, y1);
                H.drawCurve(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2(x, y)}, stroke, strokeRadius, border);

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'A') {
                float rx = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(),
                        ry = Float.parseFloat(params.get(1)) / height * pixmap.getHeight(),
                        x_axis_rotation = Float.parseFloat(params.get(2)),
                        x = Float.parseFloat(params.get(5)) / width * pixmap.getWidth(),
                        y = Float.parseFloat(params.get(6)) / height * pixmap.getHeight();
                int large_arc_flag = Math.abs(Integer.parseInt(params.get(3))),
                        sweep_flag = Math.abs(Integer.parseInt(params.get(4)));
                List<Vector2[]> curves = SvgArcToCubicBezier.arcToBezier(currentPosition.x, currentPosition.y, x, y, rx, ry, x_axis_rotation, large_arc_flag, sweep_flag);
                for (Vector2[] curve : curves) {
                    /*
                    String cmd = "M " + currentPosition.x + " " + currentPosition.y + " " +
                            "C " + curve[0].x + " " + curve[0].y + " " + curve[1].x + " " + curve[1].y + " " + curve[2].x + " " + curve[2].y + " ";
                    path2Pixmap(pixmap.getWidth(), pixmap.getHeight(), cmd, fill, stroke, strokeRadius, pixmap);
                    */
                    ArrayList<Vector2> points = new ArrayList<>(4);
                    points.add(currentPosition);
                    points.addAll(Arrays.asList(curve));
                    H.drawCurve(pixmap, points.toArray(new Vector2[4]), stroke, strokeRadius, border);
                    currentPosition.x = curve[2].x;
                    currentPosition.y = curve[2].y;
                }

                currentPosition.x = x;
                currentPosition.y = y;
            }

            // Clear useless control points
            if (newCommand != 'Q' && newCommand != 'T') lastQPoint = null;
            if (newCommand != 'C' && newCommand != 'S') lastCPoint = null;

            params.clear();
        }

        if (fill != null && !fill.equals(Color.CLEAR)) {
            H.fillColor(pixmap, border, fill);
        }

        return pixmap;
    }

    /**
     * Parse a SVG file to a Pixmap.
     *
     * @param fileContent SVG file
     * @param width       The width to Pixmap.
     * @param height      The height of Pixmap.
     */
    public static Pixmap svg2Pixmap(String fileContent, int width, int height) {
        checkGWT();

        if (generateScale == 1) return svg2PixmapDirectDraw(fileContent, width, height);

        final int scaledWidth = width * generateScale, scaledHeight = height * generateScale,
                scale2 = generateScale * generateScale;
        final Pixmap scaledPixmap = svg2PixmapDirectDraw(fileContent, scaledWidth, scaledHeight),
                pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        AtomicInteger count = new AtomicInteger(0);
        for (int x = 0; x < width; x++) {
            final int x0 = x;
            H.asyncExecutor.submit(() -> {
                Color tmpColor = new Color();
                float r, g, b, a;
                for (int y = 0; y < height; y++) {
                    final int y0 = y;
                    r = g = b = a = 0;
                    for (int i = 0; i < generateScale; i++) {
                        for (int j = 0; j < generateScale; j++) {
                            int color = scaledPixmap.getPixel(x0 * generateScale + i, y0 * generateScale + j);
                            Color.rgba8888ToColor(tmpColor, color);
                            r += tmpColor.r;
                            g += tmpColor.g;
                            b += tmpColor.b;
                            a += tmpColor.a;
                        }
                    }
                    r /= scale2;
                    g /= scale2;
                    b /= scale2;
                    a /= scale2;
                    pixmap.drawPixel(x0, y0, Color.rgba8888(r, g, b, a));
                }
                count.incrementAndGet();
                return null;
            });
        }
        while (count.get() < width) {
            ThreadUtils.yield();
        }
        scaledPixmap.dispose();
        return pixmap;
    }

    /**
     * Convert SVG file to Pixmap using browser apis in GWT mode.
     * Obviously, do not call this on other backends.
     *
     * @param callback due to image loading limitations, results cannot be provided instantly,
     *                 a callback function/method is required to return the result.
     * @see ICallback
     */
    // @off
    // @formatter:off
    public static native void svg2PixmapJSNI(String fileContent, int width, int height, ICallback callback)/*-{
        var img = new Image();
        img.src = 'data:image/svg+xml; charset=utf8, ' + encodeURIComponent(fileContent);
        img.width = width;
        img.height = height;
        img.onload = function(){
            var pixmap = @com.badlogic.gdx.graphics.Pixmap::new(Lcom/google/gwt/dom/client/ImageElement;)(img);
            callback.@io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap.ICallback::onload(Lcom/badlogic/gdx/graphics/Pixmap;)(pixmap);
        }
    }-*/;
    // @on
    // @formatter:on

    public static Pixmap svg2PixmapDirectDraw(String fileContent, int width, int height) {
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(fileContent);

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element child = root.getChild(i);
            try {
                String name = child.getName();
                switch (name) {
                    case "path":
                        path(child, pixmap);
                        break;
                    case "circle":
                        circle(child, pixmap);
                        break;
                    case "ellipse":
                        ellipse(child, pixmap);
                        break;
                    default:
                        Gdx.app.error("svg2PixmapDirectDraw", "Unsupported element " + name);
                }
            } catch (Exception e) { //TODO Dangerous here !!!
                Gdx.app.debug("Svg2Pixmap", "File content:\n" + fileContent + "\nError stacktrace: ", e);
            }
        }
        return pixmap;
    }

    public static Pixmap svg2Pixmap(String fileContent) {
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(fileContent);
        int width = Integer.parseInt(root.getAttribute("width"));
        int height = Integer.parseInt(root.getAttribute("height"));
        return svg2Pixmap(fileContent, width, height);
    }

    public static void path(XmlReader.Element element, Pixmap pixmap) {
        H.SVGBasicInfo info = new H.SVGBasicInfo(element);
        String d = H.getAttribute(element, "d");

        path2Pixmap(info.width, info.height, d, info.fill, info.stroke, info.strokeWidth, pixmap);
    }

    public static void circle(XmlReader.Element element, Pixmap pixmap) {
        H.SVGBasicInfo info = new H.SVGBasicInfo(element);
        double cx = Double.parseDouble(H.getAttribute(element, "cx")),
                cy = Double.parseDouble(H.getAttribute(element, "cy")),
                r = Double.parseDouble(H.getAttribute(element, "r"));

        String d = "M " + (cx - r) + " " + cy + " " +
                "A " + r + " " + r + " 0 1 1 " + (cx + r) + " " + cy + " " +
                "A " + r + " " + r + " 0 1 1 " + (cx - r) + " " + cy + " ";
        path2Pixmap(info.width, info.height, d, info.fill, info.stroke, info.strokeWidth, pixmap);
    }

    public static void ellipse(XmlReader.Element element, Pixmap pixmap) {
        H.SVGBasicInfo info = new H.SVGBasicInfo(element);
        double cx = Double.parseDouble(H.getAttribute(element, "cx")),
                cy = Double.parseDouble(H.getAttribute(element, "cy")),
                rx = Double.parseDouble(H.getAttribute(element, "rx")),
                ry = Double.parseDouble(H.getAttribute(element, "ry"));

        String d = "M " + (cx - rx) + " " + cy + " " +
                "A " + rx + " " + ry + " 0 1 1 " + (cx + rx) + " " + cy + " " +
                "A " + rx + " " + ry + " 0 1 1 " + (cx - rx) + " " + cy + " ";
        path2Pixmap(info.width, info.height, d, info.fill, info.stroke, info.strokeWidth, pixmap);
    }

    protected static void checkGWT() {
        if (Gdx.app.getType().equals(Application.ApplicationType.WebGL)) {
            Gdx.app.error("Svg2Pixmap", "Due to performance issue, in GWT mode please use functions with suffix -JSNI instead.");
        }
    }

    /**
     * @see Svg2Pixmap#svg2PixmapJSNI(String, int, int, ICallback)
     */
    public interface ICallback {
        void onload(Pixmap pixmap);
    }

}