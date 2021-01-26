package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.XmlReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Svg2Pixmap {

    /**
     * Convert a SVG {@code <path />} element into a {@link Pixmap}.
     * Will scale if (width != {@link Pixmap#getWidth()} || height != {@link Pixmap#getHeight()}).
     *
     * @param width       the origin width of the SVG file.
     * @param height      the origin height of the SVG file.
     * @param d           the property d of the origin path element
     * @param fill        the color to fill in the shape.
     * @param stroke      the color of th path
     * @param strokeWidth width to draw. WARNING: WILL BE SCALED if (width != {@link Pixmap#getWidth()} || height != {@link Pixmap#getHeight()}).
     * @param pixmap      the pixmap to draw the path in.
     * @return Drawn pixmap.
     */
    public static Pixmap path2Pixmap(int width, int height, String d, Color fill, Color stroke, double strokeWidth, Pixmap pixmap) {
        StringTokenizer stringTokenizer = new StringTokenizer(H.splitMixedTokens(d));
        // TODO strokeWidth not correct with batik
        strokeWidth = strokeWidth * Math.sqrt(1.0 * (pixmap.getWidth() * pixmap.getHeight()) / (width * height));

        Vector2 currentPosition = new Vector2(0, 0);// Current position. Used by commands.
        Vector2 initialPoint = new Vector2(0, 0);// Current position. Used by command 'M'.
        Vector2 lastCPoint = null; // Last control point of last 'C' or 'S' command.
        Vector2 lastQPoint = null; // Last control point of last 'Q' or 'T' command.

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

            H.r2a(command, params, new Vector2(currentPosition.x / pixmap.getWidth() * width, currentPosition.y / pixmap.getHeight() * height));
            char newCommand = Character.toUpperCase(command);
            if (newCommand == 'M') {
                initialPoint.x = currentPosition.x = (Float.parseFloat(params.get(0))) / width * pixmap.getWidth();
                initialPoint.y = currentPosition.y = (Float.parseFloat(params.get(1))) / height * pixmap.getHeight();
            }
            if (newCommand == 'Z') {
                H.curveTo(pixmap, new Vector2[]{currentPosition, initialPoint}, stroke, (int) Math.round(strokeWidth));
            }
            if (newCommand == 'L') {
                float x2 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y2 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x2, y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x2;
                currentPosition.y = y2;
            }
            if (newCommand == 'H') {
                float x2 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x2, currentPosition.y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x2;
            }
            if (newCommand == 'V') {
                float y2 = Float.parseFloat(params.get(0)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(currentPosition.x, y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.y = y2;
            }
            if (newCommand == 'C') {
                float x1 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y1 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x2 = Float.parseFloat(params.get(2)) / width * pixmap.getWidth(), y2 = Float.parseFloat(params.get(3)) / height * pixmap.getHeight();
                float x = Float.parseFloat(params.get(4)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(5)) / height * pixmap.getHeight();
                lastCPoint = new Vector2(x2, y2);
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x1, y1), lastCPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

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
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x1, y1), lastCPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'Q') {
                float x1 = Float.parseFloat(params.get(0)) / width * pixmap.getWidth(), y1 = Float.parseFloat(params.get(1)) / height * pixmap.getHeight();
                float x = Float.parseFloat(params.get(2)) / width * pixmap.getWidth(), y = Float.parseFloat(params.get(3)) / height * pixmap.getHeight();
                lastQPoint = new Vector2(x1, y1);
                H.curveTo(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

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
                H.curveTo(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

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
                    path2Pixmap(pixmap.getWidth(), pixmap.getHeight(), cmd, fill, stroke, strokeWidth, pixmap);
                    */
                    ArrayList<Vector2> points = new ArrayList<>(4);
                    points.add(currentPosition);
                    points.addAll(Arrays.asList(curve));
                    H.curveTo(pixmap, points.toArray(new Vector2[4]), stroke, (int) Math.round(strokeWidth));
                    currentPosition.x = curve[2].x;
                    currentPosition.y = curve[2].y;
                }

                currentPosition.x = x;
                currentPosition.y = y;
            }

            //TODO implement fill

            // Clear useless control points
            if (newCommand != 'Q' && newCommand != 'T') lastQPoint = null;
            if (newCommand != 'C' && newCommand != 'S') lastCPoint = null;

            params.clear();
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
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(fileContent);
        double strokeWidth = -1;

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        for (int i = 0; i < root.getChildCount(); i++) {
            XmlReader.Element child = root.getChild(i);
            try {
                if (child.getName().equals("path"))
                    path(child, pixmap);
                else if (child.getName().equals("circle"))
                    circle(child, pixmap);
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
        int width = Integer.parseInt(H.getAttribute(element, "width"));
        int height = Integer.parseInt(H.getAttribute(element, "height"));
        Color fill = Color.BLACK, stroke = Color.BLACK;
        try {
            fill = H.svgReadColor(H.getAttribute(element, "fill"), fill);
        } catch (NullPointerException ignored) {
        }
        try {
            stroke = H.svgReadColor(H.getAttribute(element, "stroke"), stroke);
        } catch (NullPointerException ignored) {
        } catch (GdxRuntimeException gre) {
            stroke = Color.CLEAR;
        }
        path2Pixmap(width, height, H.getAttribute(element, "d"), fill, stroke, H.svgReadDouble(H.getAttribute(element, "stroke-width"), Math.sqrt(width * width + height * height)), pixmap);
    }

    public static void circle(XmlReader.Element element, Pixmap pixmap) {
        int width = Integer.parseInt(H.getAttribute(element, "width"));
        int height = Integer.parseInt(H.getAttribute(element, "height"));
        Color fill = Color.BLACK, stroke = Color.BLACK;
        try {
            fill = H.svgReadColor(H.getAttribute(element, "fill"), fill);
        } catch (NullPointerException ignored) {
        }
        try {
            stroke = H.svgReadColor(H.getAttribute(element, "stroke"), stroke);
        } catch (NullPointerException ignored) {
        } catch (GdxRuntimeException gre) {
            stroke = Color.CLEAR;
        }
        double cx = Double.parseDouble(H.getAttribute(element, "cx")), cy = Double.parseDouble(H.getAttribute(element, "cy")), r = Double.parseDouble(H.getAttribute(element, "r"));

        String d = "M " + (cx - r) + " " + cy + " " +
                "A " + r + " " + r + " 0 1 1 " + (cx + r) + " " + cy + " " +
                "A " + r + " " + r + " 0 1 1 " + (cx - r) + " " + cy + " ";
        path2Pixmap(width, height, d, fill, stroke, H.svgReadDouble(H.getAttribute(element, "stroke-width"), Math.sqrt(width * width + height * height)), pixmap);
    }

}