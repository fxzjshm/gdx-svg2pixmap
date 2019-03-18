package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader;

import java.util.LinkedHashMap;
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
        strokeWidth = strokeWidth * Math.sqrt((pixmap.getWidth() * pixmap.getHeight()) / (width * height));

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
                initialPoint.x = currentPosition.x = (Float.valueOf(params.get(0))) / width * pixmap.getWidth();
                initialPoint.y = currentPosition.y = (Float.valueOf(params.get(1))) / height * pixmap.getHeight();
            }
            if (newCommand == 'Z') {
                H.curveTo(pixmap, new Vector2[]{currentPosition, initialPoint}, stroke, (int) Math.round(strokeWidth));
            }
            if (newCommand == 'L') {
                float x2 = Float.valueOf(params.get(0)) / width * pixmap.getWidth(), y2 = Float.valueOf(params.get(1)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x2, y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x2;
                currentPosition.y = y2;
            }
            if (newCommand == 'H') {
                float x2 = Float.valueOf(params.get(0)) / width * pixmap.getWidth();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x2, currentPosition.y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x2;
            }
            if (newCommand == 'V') {
                float y2 = Float.valueOf(params.get(0)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(currentPosition.x, y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.y = y2;
            }
            if (newCommand == 'C') {
                float x1 = Float.valueOf(params.get(0)) / width * pixmap.getWidth(), y1 = Float.valueOf(params.get(1)) / height * pixmap.getHeight();
                float x2 = Float.valueOf(params.get(2)) / width * pixmap.getWidth(), y2 = Float.valueOf(params.get(3)) / height * pixmap.getHeight();
                float x = Float.valueOf(params.get(4)) / width * pixmap.getWidth(), y = Float.valueOf(params.get(5)) / height * pixmap.getHeight();
                lastCPoint = new Vector2(x2, y2);
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(x1, y1), lastCPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'S') {
                float x2 = Float.valueOf(params.get(0)) / width * pixmap.getWidth(), y2 = Float.valueOf(params.get(1)) / height * pixmap.getHeight();
                float x = Float.valueOf(params.get(2)) / width * pixmap.getWidth(), y = Float.valueOf(params.get(3)) / height * pixmap.getHeight();
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
                float x1 = Float.valueOf(params.get(0)) / width * pixmap.getWidth(), y1 = Float.valueOf(params.get(1)) / height * pixmap.getHeight();
                float x = Float.valueOf(params.get(2)) / width * pixmap.getWidth(), y = Float.valueOf(params.get(3)) / height * pixmap.getHeight();
                lastQPoint = new Vector2(x1, y1);
                H.curveTo(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2(x, y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = x;
                currentPosition.y = y;
            }
            if (newCommand == 'T') {
                float x = Float.valueOf(params.get(0)) / width * pixmap.getWidth(), y = Float.valueOf(params.get(1)) / height * pixmap.getHeight();
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
                float rx = Float.valueOf(params.get(0)), ry = Float.valueOf(params.get(1)), x_axis_rotation = Float.valueOf(params.get(2)), x = Float.valueOf(params.get(5)), y = Float.valueOf(params.get(6));
                boolean large_arc_flag = Double.valueOf(params.get(3)) == 1, sweep_flag = Double.valueOf(params.get(4)) == 1;
                H.ConvertArcToCurves(currentPosition.x, currentPosition.y, x, y, rx, ry, x_axis_rotation, large_arc_flag, sweep_flag, pixmap, stroke, (int) Math.round(strokeWidth));

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
                Gdx.app.debug("Svg2Pixmap", "File:\n" + fileContent, e);
            }
        }
        return pixmap;
    }

    public static Pixmap svg2Pixmap(String fileContent) {
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(fileContent);
        int width = Integer.valueOf(root.getAttribute("width"));
        int height = Integer.valueOf(root.getAttribute("height"));
        return svg2Pixmap(fileContent, width, height);
    }

    public static void path(XmlReader.Element element, Pixmap pixmap) {
        int width = Integer.valueOf(H.getAttribute(element, "width"));
        int height = Integer.valueOf(H.getAttribute(element, "height"));
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
        path2Pixmap(width, height, element.getAttribute("d"), fill, stroke, H.svgReadDouble(H.getAttribute(element, "stroke-width"), Math.sqrt(width * width + height * height)), pixmap);
    }

    public static void circle(XmlReader.Element element, Pixmap pixmap) {
        int width = Integer.valueOf(H.getAttribute(element, "width"));
        int height = Integer.valueOf(H.getAttribute(element, "height"));
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
        double cx = Double.valueOf(H.getAttribute(element, "cx")), cy = Double.valueOf(H.getAttribute(element, "cy")), r = Double.valueOf(H.getAttribute(element, "r"));
        String d = "M " + cx + " " + cy + " " +
                "m " + -r + " 0 " +
                "a " + r + " " + r + " 0 1 1 " + r * 2 + " 0 " +
                "a " + r + " " + r + " 0 1 1 " + -r * 2 + " 0 ";
        path2Pixmap(width, height, d, fill, stroke, H.svgReadDouble(H.getAttribute(element, "stroke-width"), Math.sqrt(width * width + height * height)), pixmap);
    }

    /**
     * Helpful(-less) methods.
     */
    public static class H {

        public static LinkedHashMap<String, Color> colorMap = new LinkedHashMap<String, Color>(150);

        static {
            colorMap.put("aliceblue", H.color8BitsToFloat(240, 248, 255));
            colorMap.put("antiquewhite", H.color8BitsToFloat(250, 235, 215));
            colorMap.put("aqua", H.color8BitsToFloat(0, 255, 255));
            colorMap.put("aquamarine", H.color8BitsToFloat(127, 255, 212));
            colorMap.put("azure", H.color8BitsToFloat(240, 255, 255));
            colorMap.put("beige", H.color8BitsToFloat(245, 245, 220));
            colorMap.put("bisque", H.color8BitsToFloat(255, 228, 196));
            colorMap.put("black", H.color8BitsToFloat(0, 0, 0));
            colorMap.put("blanchedalmond", H.color8BitsToFloat(255, 235, 205));
            colorMap.put("blue", H.color8BitsToFloat(0, 0, 255));
            colorMap.put("blueviolet", H.color8BitsToFloat(138, 43, 226));
            colorMap.put("brown", H.color8BitsToFloat(165, 42, 42));
            colorMap.put("burlywood", H.color8BitsToFloat(222, 184, 135));
            colorMap.put("cadetblue", H.color8BitsToFloat(95, 158, 160));
            colorMap.put("chartreuse", H.color8BitsToFloat(127, 255, 0));
            colorMap.put("chocolate", H.color8BitsToFloat(210, 105, 30));
            colorMap.put("coral", H.color8BitsToFloat(255, 127, 80));
            colorMap.put("cornflowerblue", H.color8BitsToFloat(100, 149, 237));
            colorMap.put("cornsilk", H.color8BitsToFloat(255, 248, 220));
            colorMap.put("crimson", H.color8BitsToFloat(220, 20, 60));
            colorMap.put("cyan", H.color8BitsToFloat(0, 255, 255));
            colorMap.put("darkblue", H.color8BitsToFloat(0, 0, 139));
            colorMap.put("darkcyan", H.color8BitsToFloat(0, 139, 139));
            colorMap.put("darkgoldenrod", H.color8BitsToFloat(184, 134, 11));
            colorMap.put("darkgray", H.color8BitsToFloat(169, 169, 169));
            colorMap.put("darkgreen", H.color8BitsToFloat(0, 100, 0));
            colorMap.put("darkgrey", H.color8BitsToFloat(169, 169, 169));
            colorMap.put("darkkhaki", H.color8BitsToFloat(189, 183, 107));
            colorMap.put("darkmagenta", H.color8BitsToFloat(139, 0, 139));
            colorMap.put("darkolivegreen", H.color8BitsToFloat(85, 107, 47));
            colorMap.put("darkorange", H.color8BitsToFloat(255, 140, 0));
            colorMap.put("darkorchid", H.color8BitsToFloat(153, 50, 204));
            colorMap.put("darkred", H.color8BitsToFloat(139, 0, 0));
            colorMap.put("darksalmon", H.color8BitsToFloat(233, 150, 122));
            colorMap.put("darkseagreen", H.color8BitsToFloat(143, 188, 143));
            colorMap.put("darkslateblue", H.color8BitsToFloat(72, 61, 139));
            colorMap.put("darkslategray", H.color8BitsToFloat(47, 79, 79));
            colorMap.put("darkslategrey", H.color8BitsToFloat(47, 79, 79));
            colorMap.put("darkturquoise", H.color8BitsToFloat(0, 206, 209));
            colorMap.put("darkviolet", H.color8BitsToFloat(148, 0, 211));
            colorMap.put("deeppink", H.color8BitsToFloat(255, 20, 147));
            colorMap.put("deepskyblue", H.color8BitsToFloat(0, 191, 255));
            colorMap.put("dimgray", H.color8BitsToFloat(105, 105, 105));
            colorMap.put("dimgrey", H.color8BitsToFloat(105, 105, 105));
            colorMap.put("dodgerblue", H.color8BitsToFloat(30, 144, 255));
            colorMap.put("firebrick", H.color8BitsToFloat(178, 34, 34));
            colorMap.put("floralwhite", H.color8BitsToFloat(255, 250, 240));
            colorMap.put("forestgreen", H.color8BitsToFloat(34, 139, 34));
            colorMap.put("fuchsia", H.color8BitsToFloat(255, 0, 255));
            colorMap.put("gainsboro", H.color8BitsToFloat(220, 220, 220));
            colorMap.put("ghostwhite", H.color8BitsToFloat(248, 248, 255));
            colorMap.put("gold", H.color8BitsToFloat(255, 215, 0));
            colorMap.put("goldenrod", H.color8BitsToFloat(218, 165, 32));
            colorMap.put("gray", H.color8BitsToFloat(128, 128, 128));
            colorMap.put("grey", H.color8BitsToFloat(128, 128, 128));
            colorMap.put("green", H.color8BitsToFloat(0, 128, 0));
            colorMap.put("greenyellow", H.color8BitsToFloat(173, 255, 47));
            colorMap.put("honeydew", H.color8BitsToFloat(240, 255, 240));
            colorMap.put("hotpink", H.color8BitsToFloat(255, 105, 180));
            colorMap.put("indianred", H.color8BitsToFloat(205, 92, 92));
            colorMap.put("indigo", H.color8BitsToFloat(75, 0, 130));
            colorMap.put("ivory", H.color8BitsToFloat(255, 255, 240));
            colorMap.put("khaki", H.color8BitsToFloat(240, 230, 140));
            colorMap.put("lavender", H.color8BitsToFloat(230, 230, 250));
            colorMap.put("lavenderblush", H.color8BitsToFloat(255, 240, 245));
            colorMap.put("lawngreen", H.color8BitsToFloat(124, 252, 0));
            colorMap.put("lemonchiffon", H.color8BitsToFloat(255, 250, 205));
            colorMap.put("lightblue", H.color8BitsToFloat(173, 216, 230));
            colorMap.put("lightcoral", H.color8BitsToFloat(240, 128, 128));
            colorMap.put("lightcyan", H.color8BitsToFloat(224, 255, 255));
            colorMap.put("lightgoldenrodyellow", H.color8BitsToFloat(250, 250, 210));
            colorMap.put("lightgray", H.color8BitsToFloat(211, 211, 211));
            colorMap.put("lightgreen", H.color8BitsToFloat(144, 238, 144));
            colorMap.put("lightgrey", H.color8BitsToFloat(211, 211, 211));
            colorMap.put("lightpink", H.color8BitsToFloat(255, 182, 193));
            colorMap.put("lightsalmon", H.color8BitsToFloat(255, 160, 122));
            colorMap.put("lightseagreen", H.color8BitsToFloat(32, 178, 170));
            colorMap.put("lightskyblue", H.color8BitsToFloat(135, 206, 250));
            colorMap.put("lightslategray", H.color8BitsToFloat(119, 136, 153));
            colorMap.put("lightslategrey", H.color8BitsToFloat(119, 136, 153));
            colorMap.put("lightsteelblue", H.color8BitsToFloat(176, 196, 222));
            colorMap.put("lightyellow", H.color8BitsToFloat(255, 255, 224));
            colorMap.put("lime", H.color8BitsToFloat(0, 255, 0));
            colorMap.put("limegreen", H.color8BitsToFloat(50, 205, 50));
            colorMap.put("linen", H.color8BitsToFloat(250, 240, 230));
            colorMap.put("magenta", H.color8BitsToFloat(255, 0, 255));
            colorMap.put("maroon", H.color8BitsToFloat(128, 0, 0));
            colorMap.put("mediumaquamarine", H.color8BitsToFloat(102, 205, 170));
            colorMap.put("mediumblue", H.color8BitsToFloat(0, 0, 205));
            colorMap.put("mediumorchid", H.color8BitsToFloat(186, 85, 211));
            colorMap.put("mediumpurple", H.color8BitsToFloat(147, 112, 219));
            colorMap.put("mediumseagreen", H.color8BitsToFloat(60, 179, 113));
            colorMap.put("mediumslateblue", H.color8BitsToFloat(123, 104, 238));
            colorMap.put("mediumspringgreen", H.color8BitsToFloat(0, 250, 154));
            colorMap.put("mediumturquoise", H.color8BitsToFloat(72, 209, 204));
            colorMap.put("mediumvioletred", H.color8BitsToFloat(199, 21, 133));
            colorMap.put("midnightblue", H.color8BitsToFloat(25, 25, 112));
            colorMap.put("mintcream", H.color8BitsToFloat(245, 255, 250));
            colorMap.put("mistyrose", H.color8BitsToFloat(255, 228, 225));
            colorMap.put("moccasin", H.color8BitsToFloat(255, 228, 181));
            colorMap.put("navajowhite", H.color8BitsToFloat(255, 222, 173));
            colorMap.put("navy", H.color8BitsToFloat(0, 0, 128));
            colorMap.put("oldlace", H.color8BitsToFloat(253, 245, 230));
            colorMap.put("olive", H.color8BitsToFloat(128, 128, 0));
            colorMap.put("olivedrab", H.color8BitsToFloat(107, 142, 35));
            colorMap.put("orange", H.color8BitsToFloat(255, 165, 0));
            colorMap.put("orangered", H.color8BitsToFloat(255, 69, 0));
            colorMap.put("orchid", H.color8BitsToFloat(218, 112, 214));
            colorMap.put("palegoldenrod", H.color8BitsToFloat(238, 232, 170));
            colorMap.put("palegreen", H.color8BitsToFloat(152, 251, 152));
            colorMap.put("paleturquoise", H.color8BitsToFloat(175, 238, 238));
            colorMap.put("palevioletred", H.color8BitsToFloat(219, 112, 147));
            colorMap.put("papayawhip", H.color8BitsToFloat(255, 239, 213));
            colorMap.put("peachpuff", H.color8BitsToFloat(255, 218, 185));
            colorMap.put("peru", H.color8BitsToFloat(205, 133, 63));
            colorMap.put("pink", H.color8BitsToFloat(255, 192, 203));
            colorMap.put("plum", H.color8BitsToFloat(221, 160, 221));
            colorMap.put("powderblue", H.color8BitsToFloat(176, 224, 230));
            colorMap.put("purple", H.color8BitsToFloat(128, 0, 128));
            colorMap.put("red", H.color8BitsToFloat(255, 0, 0));
            colorMap.put("rosybrown", H.color8BitsToFloat(188, 143, 143));
            colorMap.put("royalblue", H.color8BitsToFloat(65, 105, 225));
            colorMap.put("saddlebrown", H.color8BitsToFloat(139, 69, 19));
            colorMap.put("salmon", H.color8BitsToFloat(250, 128, 114));
            colorMap.put("sandybrown", H.color8BitsToFloat(244, 164, 96));
            colorMap.put("seagreen", H.color8BitsToFloat(46, 139, 87));
            colorMap.put("seashell", H.color8BitsToFloat(255, 245, 238));
            colorMap.put("sienna", H.color8BitsToFloat(160, 82, 45));
            colorMap.put("silver", H.color8BitsToFloat(192, 192, 192));
            colorMap.put("skyblue", H.color8BitsToFloat(135, 206, 235));
            colorMap.put("slateblue", H.color8BitsToFloat(106, 90, 205));
            colorMap.put("slategray", H.color8BitsToFloat(112, 128, 144));
            colorMap.put("slategrey", H.color8BitsToFloat(112, 128, 144));
            colorMap.put("snow", H.color8BitsToFloat(255, 250, 250));
            colorMap.put("springgreen", H.color8BitsToFloat(0, 255, 127));
            colorMap.put("steelblue", H.color8BitsToFloat(70, 130, 180));
            colorMap.put("tan", H.color8BitsToFloat(210, 180, 140));
            colorMap.put("teal", H.color8BitsToFloat(0, 128, 128));
            colorMap.put("thistle", H.color8BitsToFloat(216, 191, 216));
            colorMap.put("tomato", H.color8BitsToFloat(255, 99, 71));
            colorMap.put("transparent", H.color8BitsToFloat(0, 0, 0, 0));
            colorMap.put("turquoise", H.color8BitsToFloat(64, 224, 208));
            colorMap.put("violet", H.color8BitsToFloat(238, 130, 238));
            colorMap.put("wheat", H.color8BitsToFloat(245, 222, 179));
            colorMap.put("white", H.color8BitsToFloat(255, 255, 255));
            colorMap.put("whitesmoke", H.color8BitsToFloat(245, 245, 245));
            colorMap.put("yellow", H.color8BitsToFloat(255, 255, 0));
            colorMap.put("yellowgreen", H.color8BitsToFloat(154, 205, 50));
        }

        /**
         * Split mixed tokens.
         */
        public static String splitMixedTokens(String d) {
            // Old implement. Maybe laggy.
            /*
            d = d.replace(',', ' ');
            for (char c = 's'; c < 'z'; c++) d = d.replace(String.valueOf(c), c + " ");
            for (char c = 'A'; c < 'Z'; c++) d = d.replace(String.valueOf(c), c + " ");
            System.gc(); // Throw away old Strings.
            */

            /* New implement. Maybe a bit better.*/
            StringBuilder sb = new StringBuilder(d.length() * 2);
            for (int i = 0; i < d.length(); i++) {
                char c = d.charAt(i);
                if (c == ',') sb.append(' '); // Change ',' into ' '.
                if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) {
                    sb.append(' ');
                    sb.append(c);
                    sb.append(' ');
                } else sb.append(c);
            }
            return sb.toString();
        }

        /**
         * @return the amount of parameters of this command.
         */
        public static int getParamAmount(char c) {
            c = Character.toUpperCase(c);
            if (c == 'A') return 7;
            if (c == 'C') return 6;
            if (c == 'S' || c == 'Q') return 4;
            if (c == 'M' || c == 'L' || c == 'T') return 2;
            if (c == 'H' || c == 'V') return 1;
            if (c == 'Z') return 0;
            throw new IllegalArgumentException("Wrong command " + c);
        }

        /**
         * Change all relative position into absolute ones
         *
         * @deprecated Something went wrong ???
         */
        @Deprecated
        public static void r2a(char command, List<String> params, Vector2 currentPoint) {
            boolean isX = true;
            if (command == 'a') {
                params.set(0, String.valueOf(Double.valueOf(params.get(0)) + currentPoint.x));
                params.set(0, String.valueOf(Double.valueOf(params.get(1)) + currentPoint.x));
                params.set(0, String.valueOf(Double.valueOf(params.get(5)) + currentPoint.x));
                params.set(0, String.valueOf(Double.valueOf(params.get(6)) + currentPoint.x));
            } else if ('a' < command && command <= 'z') {
                for (int i = 0; i < params.size(); i++) {
                    double d = Double.valueOf(params.get(i));
                    if (isX) d += currentPoint.x;
                    else d += currentPoint.y;
                    isX = (!isX);
                    params.set(i, String.valueOf(d));
                }
            }
        }

        /**
         * Draw a Bezier curve.
         * See https://en.wikipedia.org/wiki/B%C3%A9zier_curve
         *
         * @param pixmap you want to draw on
         * @param points contains points that control the curve.
         */
        public static Pixmap curveTo(Pixmap pixmap, Vector2[] points, Color stroke, int strokeWidth) {
            int n = points.length - 1;
            int pointN = pixmap.getWidth() * pixmap.getHeight();
            for (double t = 0; t <= 1; t += 1.0 / pointN) {
                double x = 0, y = 0;
                for (int i = 0; i <= n; i++) {
                    double k = C(n, i) * Math.pow(1 - t, n - i) * Math.pow(t, i);
                    x += k * points[i].x;
                    y += k * points[i].y;
                    C(0, 0);
                }
                pixmap.setColor(stroke);
//                pixmap.fillCircle((int) Math.round(x), (int) Math.round(y), strokeWidth - 1);
                for (int i = 0; i < strokeWidth; i++) {
                    pixmap.drawCircle((int) Math.round(x), (int) Math.round(y), i);
                }
            }

            return pixmap;
        }

        public static double C(int x, int y) {
            if (y == 0 || y == x) return 1;
            if (y < 0 || y > x) return 0; // risk!
            return C(x - 1, y - 1) + C(x - 1, y);
        }

        /**
         * This method is translated from content/svg/content/src/nsSVGPathDataParser.h and its implement in Firefox source.
         * It is said that this is licensed under Mozilla Public License Version 1.1.
         */
        public static boolean
        ConvertArcToCurves(float mPx, float mPy, float x2, float y2,
                           float rx, float ry,
                           float angle,
                           boolean largeArcFlag,
                           boolean sweepFlag, Pixmap pixmap, Color stroke, int strokeWidth) {
            float x1 = mPx, y1 = mPy, x3 = 0, y3 = 0;
            // Treat out-of-range parameters as described in
            // http://www.w3.org/TR/SVG/implnote.html#ArcImplementationNotes

            // If the endpoints (x1, y1) and (x2, y2) are identical, then this
            // is equivalent to omitting the elliptical arc segment entirely
            if (x1 == x2 && y1 == y2) {
                return true;
            }
            /*
            // If rX = 0 or rY = 0 then this arc is treated as a straight line
            // segment (a "lineto") joining the endpoints.
            if (rx == 0.0f || ry == 0.0f) {
                return PathLineTo(x2, y2);
            }
            */
            nsSVGArcConverter converter = new nsSVGArcConverter(x1, y1, x2, y2, rx, ry, angle,
                    largeArcFlag, sweepFlag);

            while (true) {
                nsSVGArcConverter.GetNextSegmentResult result = converter.GetNextSegment(x1, y1, x2, y2, x3, y3);
                if (!result.isSuccess) break;
                // c) draw the cubic bezier:
                // boolean rv = PathCurveTo(x1, y1, x2, y2, x3, y3);
                H.curveTo(pixmap, new Vector2[]{new Vector2((float) result.x1, (float) result.y1), new Vector2((float) result.x2, (float) result.y2), new Vector2((float) result.x3, (float) result.y3)}, stroke, strokeWidth);
                // NS_ENSURE_SUCCESS(rv, rv);
            }

            return true;
        }

        public static double svgReadDouble(String s, double k) {
            if (s.endsWith("%")) return Double.valueOf(s.substring(0, s.length() - 2)) * k / 100.0;
            return Double.valueOf(s);
        }

        public static Color svgReadColor(String color, Color currentColor) {
            // TODO implement
            if (color.equals("currentcolor")) return currentColor;
            if (color.startsWith("#")) return Color.valueOf(color.replace("#", ""));
            return colorMap.get(color);
        }

        public static Color color8BitsToFloat(int r, int g, int b, int a) {
            return new Color(r / 256.0f, g / 256.0f, b / 256.0f, a / 256.0f);
        }

        public static Color color8BitsToFloat(int r, int g, int b) {
            return new Color(r / 256.0f, g / 256.0f, b / 256.0f, 1f);
        }

        public static String getAttribute(XmlReader.Element element, String attribute) {
            try {
                return element.getAttribute(attribute);
            } catch (GdxRuntimeException gre) {
                try {
                    return getAttribute(element.getParent(), attribute);
                } catch (NullPointerException npe) {
                    throw new GdxRuntimeException("No attribute \"" + attribute + "\" in elsement \"" + element.toString() + "\" or its parents");
                }
            }
        }

        /**
         * This class is translated from content/svg/content/src/nsSVGPathDataParser.h and its implement in Firefox source.
         * It is said that this is licensed under Mozilla Public License Version 1.1.
         */
        public static class nsSVGArcConverter {
            protected int mNumSegs, mSegIndex;
            protected double mTheta, mDelta, mT;
            protected double mSinPhi, mCosPhi;
            protected double mX1, mY1, mRx, mRy, mCx, mCy;

            nsSVGArcConverter(double x1, double y1, double x2, double y2, double rx, double ry, double angle, boolean largeArcFlag, boolean sweepFlag) {
                final double radPerDeg = Math.PI / 180.0;

                // If rX or rY have negative signs, these are dropped; the absolute
                // value is used instead.
                mRx = Math.abs(rx);
                mRy = Math.abs(ry);

                // Convert to center parameterization as shown in
                // http://www.w3.org/TR/SVG/implnote.html
                mSinPhi = Math.sin(angle * radPerDeg);
                mCosPhi = Math.cos(angle * radPerDeg);

                double x1dash = mCosPhi * (x1 - x2) / 2.0 + mSinPhi * (y1 - y2) / 2.0;
                double y1dash = -mSinPhi * (x1 - x2) / 2.0 + mCosPhi * (y1 - y2) / 2.0;

                double root;
                double numerator = mRx * mRx * mRy * mRy - mRx * mRx * y1dash * y1dash -
                        mRy * mRy * x1dash * x1dash;

                if (numerator < 0.0) {
                    //  If mRx , mRy and are such that there is no solution (basically,
                    //  the ellipse is not big enough to reach from (x1, y1) to (x2,
                    //  y2)) then the ellipse is scaled up uniformly until there is
                    //  exactly one solution (until the ellipse is just big enough).

                    // -> find factor s, such that numerator' with mRx'=s*mRx and
                    //    mRy'=s*mRy becomes 0 :
                    float s = (float) Math.sqrt(1.0 - numerator / (mRx * mRx * mRy * mRy));

                    mRx *= s;
                    mRy *= s;
                    root = 0.0;

                } else {
                    root = (largeArcFlag == sweepFlag ? -1.0 : 1.0) *
                            Math.sqrt(numerator / (mRx * mRx * y1dash * y1dash + mRy * mRy * x1dash * x1dash));
                }

                double cxdash = root * mRx * y1dash / mRy;
                double cydash = -root * mRy * x1dash / mRx;

                mCx = mCosPhi * cxdash - mSinPhi * cydash + (x1 + x2) / 2.0;
                mCy = mSinPhi * cxdash + mCosPhi * cydash + (y1 + y2) / 2.0;
                mTheta = CalcVectorAngle(1.0, 0.0, (x1dash - cxdash) / mRx, (y1dash - cydash) / mRy);
                double dtheta = CalcVectorAngle((x1dash - cxdash) / mRx, (y1dash - cydash) / mRy,
                        (-x1dash - cxdash) / mRx, (-y1dash - cydash) / mRy);
                if (!sweepFlag && dtheta > 0)
                    dtheta -= 2.0 * Math.PI;
                else if (sweepFlag && dtheta < 0)
                    dtheta += 2.0 * Math.PI;

                // Convert into cubic bezier segments <= 90deg
                mNumSegs = (int) (Math.ceil(Math.abs(dtheta / (Math.PI / 2.0))));
                mDelta = dtheta / mNumSegs;
                mT = 8.0 / 3.0 * Math.sin(mDelta / 4.0) * Math.sin(mDelta / 4.0) / Math.sin(mDelta / 2.0);

                mX1 = x1;
                mY1 = y1;
                mSegIndex = 0;
            }

            static double
            CalcVectorAngle(double ux, double uy, double vx, double vy) {
                double ta = Math.atan2(uy, ux);
                double tb = Math.atan2(vy, vx);
                if (tb >= ta)
                    return tb - ta;
                return 2 * Math.PI - (ta - tb);
            }

            GetNextSegmentResult
            GetNextSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
                if (mSegIndex == mNumSegs) {
                    return new GetNextSegmentResult(false, x1, y1, x2, y2, x3, y3);
                }

                double cosTheta1 = Math.cos(mTheta);
                double sinTheta1 = Math.sin(mTheta);
                double theta2 = mTheta + mDelta;
                double cosTheta2 = Math.cos(theta2);
                double sinTheta2 = Math.sin(theta2);

                // a) calculate endpoint of the segment:
                x3 = mCosPhi * mRx * cosTheta2 - mSinPhi * mRy * sinTheta2 + mCx;
                y3 = mSinPhi * mRx * cosTheta2 + mCosPhi * mRy * sinTheta2 + mCy;

                // b) calculate gradients at start/end points of segment:
                x1 = mX1 + mT * (-mCosPhi * mRx * sinTheta1 - mSinPhi * mRy * cosTheta1);
                y1 = mY1 + mT * (-mSinPhi * mRx * sinTheta1 + mCosPhi * mRy * cosTheta1);

                x2 = x3 + mT * (mCosPhi * mRx * sinTheta2 + mSinPhi * mRy * cosTheta2);
                y2 = y3 + mT * (mSinPhi * mRx * sinTheta2 - mCosPhi * mRy * cosTheta2);

                // do next segment
                mTheta = theta2;
                mX1 = x3;
                mY1 = y3;
                ++mSegIndex;

                return new GetNextSegmentResult(true, x1, y1, x2, y2, x3, y3);
            }

            public static class GetNextSegmentResult {
                double x1, y1, x2, y2, x3, y3;
                boolean isSuccess;

                public GetNextSegmentResult(boolean isSuccess, double x1, double y1, double x2, double y2, double x3, double y3) {
                    this.isSuccess = isSuccess;
                    this.x1 = x1;
                    this.y1 = y1;
                    this.x2 = x2;
                    this.y2 = y2;
                    this.x3 = x3;
                    this.y3 = y3;
                }
            }
        }
    }
}