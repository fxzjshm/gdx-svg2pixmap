package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Queue;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.async.AsyncExecutor;

import java.util.LinkedHashMap;
import java.util.List;

import static io.github.fxzjshm.gdx.svg2pixmap.Svg2Pixmap.defaultColor;

/**
 * Helpful(-less) methods.
 */
public class H {

    public static LinkedHashMap<String, Color> colorMap = new LinkedHashMap<>(150);

    static AsyncExecutor asyncExecutor = new AsyncExecutor(Runtime.getRuntime().availableProcessors());

    static {
        colorMap.put("aliceblue", color8BitsToFloat(240, 248, 255));
        colorMap.put("antiquewhite", color8BitsToFloat(250, 235, 215));
        colorMap.put("aqua", color8BitsToFloat(0, 255, 255));
        colorMap.put("aquamarine", color8BitsToFloat(127, 255, 212));
        colorMap.put("azure", color8BitsToFloat(240, 255, 255));
        colorMap.put("beige", color8BitsToFloat(245, 245, 220));
        colorMap.put("bisque", color8BitsToFloat(255, 228, 196));
        colorMap.put("black", color8BitsToFloat(0, 0, 0));
        colorMap.put("blanchedalmond", color8BitsToFloat(255, 235, 205));
        colorMap.put("blue", color8BitsToFloat(0, 0, 255));
        colorMap.put("blueviolet", color8BitsToFloat(138, 43, 226));
        colorMap.put("brown", color8BitsToFloat(165, 42, 42));
        colorMap.put("burlywood", color8BitsToFloat(222, 184, 135));
        colorMap.put("cadetblue", color8BitsToFloat(95, 158, 160));
        colorMap.put("chartreuse", color8BitsToFloat(127, 255, 0));
        colorMap.put("chocolate", color8BitsToFloat(210, 105, 30));
        colorMap.put("coral", color8BitsToFloat(255, 127, 80));
        colorMap.put("cornflowerblue", color8BitsToFloat(100, 149, 237));
        colorMap.put("cornsilk", color8BitsToFloat(255, 248, 220));
        colorMap.put("crimson", color8BitsToFloat(220, 20, 60));
        colorMap.put("cyan", color8BitsToFloat(0, 255, 255));
        colorMap.put("darkblue", color8BitsToFloat(0, 0, 139));
        colorMap.put("darkcyan", color8BitsToFloat(0, 139, 139));
        colorMap.put("darkgoldenrod", color8BitsToFloat(184, 134, 11));
        colorMap.put("darkgray", color8BitsToFloat(169, 169, 169));
        colorMap.put("darkgreen", color8BitsToFloat(0, 100, 0));
        colorMap.put("darkgrey", color8BitsToFloat(169, 169, 169));
        colorMap.put("darkkhaki", color8BitsToFloat(189, 183, 107));
        colorMap.put("darkmagenta", color8BitsToFloat(139, 0, 139));
        colorMap.put("darkolivegreen", color8BitsToFloat(85, 107, 47));
        colorMap.put("darkorange", color8BitsToFloat(255, 140, 0));
        colorMap.put("darkorchid", color8BitsToFloat(153, 50, 204));
        colorMap.put("darkred", color8BitsToFloat(139, 0, 0));
        colorMap.put("darksalmon", color8BitsToFloat(233, 150, 122));
        colorMap.put("darkseagreen", color8BitsToFloat(143, 188, 143));
        colorMap.put("darkslateblue", color8BitsToFloat(72, 61, 139));
        colorMap.put("darkslategray", color8BitsToFloat(47, 79, 79));
        colorMap.put("darkslategrey", color8BitsToFloat(47, 79, 79));
        colorMap.put("darkturquoise", color8BitsToFloat(0, 206, 209));
        colorMap.put("darkviolet", color8BitsToFloat(148, 0, 211));
        colorMap.put("deeppink", color8BitsToFloat(255, 20, 147));
        colorMap.put("deepskyblue", color8BitsToFloat(0, 191, 255));
        colorMap.put("dimgray", color8BitsToFloat(105, 105, 105));
        colorMap.put("dimgrey", color8BitsToFloat(105, 105, 105));
        colorMap.put("dodgerblue", color8BitsToFloat(30, 144, 255));
        colorMap.put("firebrick", color8BitsToFloat(178, 34, 34));
        colorMap.put("floralwhite", color8BitsToFloat(255, 250, 240));
        colorMap.put("forestgreen", color8BitsToFloat(34, 139, 34));
        colorMap.put("fuchsia", color8BitsToFloat(255, 0, 255));
        colorMap.put("gainsboro", color8BitsToFloat(220, 220, 220));
        colorMap.put("ghostwhite", color8BitsToFloat(248, 248, 255));
        colorMap.put("gold", color8BitsToFloat(255, 215, 0));
        colorMap.put("goldenrod", color8BitsToFloat(218, 165, 32));
        colorMap.put("gray", color8BitsToFloat(128, 128, 128));
        colorMap.put("grey", color8BitsToFloat(128, 128, 128));
        colorMap.put("green", color8BitsToFloat(0, 128, 0));
        colorMap.put("greenyellow", color8BitsToFloat(173, 255, 47));
        colorMap.put("honeydew", color8BitsToFloat(240, 255, 240));
        colorMap.put("hotpink", color8BitsToFloat(255, 105, 180));
        colorMap.put("indianred", color8BitsToFloat(205, 92, 92));
        colorMap.put("indigo", color8BitsToFloat(75, 0, 130));
        colorMap.put("ivory", color8BitsToFloat(255, 255, 240));
        colorMap.put("khaki", color8BitsToFloat(240, 230, 140));
        colorMap.put("lavender", color8BitsToFloat(230, 230, 250));
        colorMap.put("lavenderblush", color8BitsToFloat(255, 240, 245));
        colorMap.put("lawngreen", color8BitsToFloat(124, 252, 0));
        colorMap.put("lemonchiffon", color8BitsToFloat(255, 250, 205));
        colorMap.put("lightblue", color8BitsToFloat(173, 216, 230));
        colorMap.put("lightcoral", color8BitsToFloat(240, 128, 128));
        colorMap.put("lightcyan", color8BitsToFloat(224, 255, 255));
        colorMap.put("lightgoldenrodyellow", color8BitsToFloat(250, 250, 210));
        colorMap.put("lightgray", color8BitsToFloat(211, 211, 211));
        colorMap.put("lightgreen", color8BitsToFloat(144, 238, 144));
        colorMap.put("lightgrey", color8BitsToFloat(211, 211, 211));
        colorMap.put("lightpink", color8BitsToFloat(255, 182, 193));
        colorMap.put("lightsalmon", color8BitsToFloat(255, 160, 122));
        colorMap.put("lightseagreen", color8BitsToFloat(32, 178, 170));
        colorMap.put("lightskyblue", color8BitsToFloat(135, 206, 250));
        colorMap.put("lightslategray", color8BitsToFloat(119, 136, 153));
        colorMap.put("lightslategrey", color8BitsToFloat(119, 136, 153));
        colorMap.put("lightsteelblue", color8BitsToFloat(176, 196, 222));
        colorMap.put("lightyellow", color8BitsToFloat(255, 255, 224));
        colorMap.put("lime", color8BitsToFloat(0, 255, 0));
        colorMap.put("limegreen", color8BitsToFloat(50, 205, 50));
        colorMap.put("linen", color8BitsToFloat(250, 240, 230));
        colorMap.put("magenta", color8BitsToFloat(255, 0, 255));
        colorMap.put("maroon", color8BitsToFloat(128, 0, 0));
        colorMap.put("mediumaquamarine", color8BitsToFloat(102, 205, 170));
        colorMap.put("mediumblue", color8BitsToFloat(0, 0, 205));
        colorMap.put("mediumorchid", color8BitsToFloat(186, 85, 211));
        colorMap.put("mediumpurple", color8BitsToFloat(147, 112, 219));
        colorMap.put("mediumseagreen", color8BitsToFloat(60, 179, 113));
        colorMap.put("mediumslateblue", color8BitsToFloat(123, 104, 238));
        colorMap.put("mediumspringgreen", color8BitsToFloat(0, 250, 154));
        colorMap.put("mediumturquoise", color8BitsToFloat(72, 209, 204));
        colorMap.put("mediumvioletred", color8BitsToFloat(199, 21, 133));
        colorMap.put("midnightblue", color8BitsToFloat(25, 25, 112));
        colorMap.put("mintcream", color8BitsToFloat(245, 255, 250));
        colorMap.put("mistyrose", color8BitsToFloat(255, 228, 225));
        colorMap.put("moccasin", color8BitsToFloat(255, 228, 181));
        colorMap.put("navajowhite", color8BitsToFloat(255, 222, 173));
        colorMap.put("navy", color8BitsToFloat(0, 0, 128));
        colorMap.put("none", Color.CLEAR);
        colorMap.put("oldlace", color8BitsToFloat(253, 245, 230));
        colorMap.put("olive", color8BitsToFloat(128, 128, 0));
        colorMap.put("olivedrab", color8BitsToFloat(107, 142, 35));
        colorMap.put("orange", color8BitsToFloat(255, 165, 0));
        colorMap.put("orangered", color8BitsToFloat(255, 69, 0));
        colorMap.put("orchid", color8BitsToFloat(218, 112, 214));
        colorMap.put("palegoldenrod", color8BitsToFloat(238, 232, 170));
        colorMap.put("palegreen", color8BitsToFloat(152, 251, 152));
        colorMap.put("paleturquoise", color8BitsToFloat(175, 238, 238));
        colorMap.put("palevioletred", color8BitsToFloat(219, 112, 147));
        colorMap.put("papayawhip", color8BitsToFloat(255, 239, 213));
        colorMap.put("peachpuff", color8BitsToFloat(255, 218, 185));
        colorMap.put("peru", color8BitsToFloat(205, 133, 63));
        colorMap.put("pink", color8BitsToFloat(255, 192, 203));
        colorMap.put("plum", color8BitsToFloat(221, 160, 221));
        colorMap.put("powderblue", color8BitsToFloat(176, 224, 230));
        colorMap.put("purple", color8BitsToFloat(128, 0, 128));
        colorMap.put("red", color8BitsToFloat(255, 0, 0));
        colorMap.put("rosybrown", color8BitsToFloat(188, 143, 143));
        colorMap.put("royalblue", color8BitsToFloat(65, 105, 225));
        colorMap.put("saddlebrown", color8BitsToFloat(139, 69, 19));
        colorMap.put("salmon", color8BitsToFloat(250, 128, 114));
        colorMap.put("sandybrown", color8BitsToFloat(244, 164, 96));
        colorMap.put("seagreen", color8BitsToFloat(46, 139, 87));
        colorMap.put("seashell", color8BitsToFloat(255, 245, 238));
        colorMap.put("sienna", color8BitsToFloat(160, 82, 45));
        colorMap.put("silver", color8BitsToFloat(192, 192, 192));
        colorMap.put("skyblue", color8BitsToFloat(135, 206, 235));
        colorMap.put("slateblue", color8BitsToFloat(106, 90, 205));
        colorMap.put("slategray", color8BitsToFloat(112, 128, 144));
        colorMap.put("slategrey", color8BitsToFloat(112, 128, 144));
        colorMap.put("snow", color8BitsToFloat(255, 250, 250));
        colorMap.put("springgreen", color8BitsToFloat(0, 255, 127));
        colorMap.put("steelblue", color8BitsToFloat(70, 130, 180));
        colorMap.put("tan", color8BitsToFloat(210, 180, 140));
        colorMap.put("teal", color8BitsToFloat(0, 128, 128));
        colorMap.put("thistle", color8BitsToFloat(216, 191, 216));
        colorMap.put("tomato", color8BitsToFloat(255, 99, 71));
        colorMap.put("transparent", color8BitsToFloat(0, 0, 0, 0));
        colorMap.put("turquoise", color8BitsToFloat(64, 224, 208));
        colorMap.put("violet", color8BitsToFloat(238, 130, 238));
        colorMap.put("wheat", color8BitsToFloat(245, 222, 179));
        colorMap.put("white", color8BitsToFloat(255, 255, 255));
        colorMap.put("whitesmoke", color8BitsToFloat(245, 245, 245));
        colorMap.put("yellow", color8BitsToFloat(255, 255, 0));
        colorMap.put("yellowgreen", color8BitsToFloat(154, 205, 50));
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
        throw new IllegalArgumentException("Unknown command " + c);
    }

    /**
     * Change all relative position into absolute ones
     */
    public static void r2a(char command, List<String> params, Vector2 currentPoint) {
        boolean isX = true;
        if (command == 'a') {
            params.set(5, String.valueOf(Double.parseDouble(params.get(5)) + currentPoint.x));
            params.set(6, String.valueOf(Double.parseDouble(params.get(6)) + currentPoint.y));
        } else if ('a' < command && command <= 'z') {
            for (int i = 0; i < params.size(); i++) {
                double d = Double.parseDouble(params.get(i));
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
     * @see H#curveTo(Pixmap, Vector2[], Color, int, boolean[][])
     * @deprecated not recording shape of the curve
     */
    @Deprecated
    public static void curveTo(Pixmap pixmap, Vector2[] points, Color stroke, int strokeWidth) {
        curveTo(pixmap, points, stroke, strokeWidth, null);
    }

    /**
     * Draw a Bezier curve.
     * See https://en.wikipedia.org/wiki/B%C3%A9zier_curve
     *
     * @param pixmap you want to draw on
     * @param points contains points that control the curve.
     * @param border a boolean[][] to record shape of the curve, with stroke-width == 1
     *               this is designed for stroke-width == 0 situation
     * @see H#curveTo(Pixmap, Vector2[], Color, int)
     * @see H#drawCurve(Pixmap, Vector2[], Color, int, boolean[][])
     */
    public static void curveTo(Pixmap pixmap, Vector2[] points, Color stroke, int strokeWidth, @Null boolean[][] border) {
        int n = points.length - 1;
        int w = pixmap.getWidth(), h = pixmap.getHeight();
        double minx = w, maxx = 0, miny = h, maxy = 0;
        for (Vector2 p : points) {
            minx = Math.min(p.x, minx);
            maxx = Math.max(p.x, maxx);
            miny = Math.min(p.y, miny);
            maxy = Math.max(p.y, maxy);
        }
        int dx = Math.max(1, (int) Math.round(maxx - minx));
        int dy = Math.max(1, (int) Math.round(maxy - miny));
        int pointN = dx * dy;
        for (double t = 0; t <= 1; t += 1.0 / pointN) {
            double x = 0, y = 0;
            for (int i = 0; i <= n; i++) {
                double k = c[n][i] * Math.pow(1 - t, n - i) * Math.pow(t, i);
                x += k * points[i].x;
                y += k * points[i].y;
            }
            int p = (int) Math.round(x), q = (int) Math.round(y);
            pixmap.setColor(stroke);
            // pixmap.fillCircle((int) Math.round(x), (int) Math.round(y), strokeWidth); // this performs worse?
            for (int i = 0; i < strokeWidth; i++) {
                pixmap.drawCircle(p, q, i);
            }
            p = Math.max(0, p);
            p = Math.min(w - 1, p);
            q = Math.max(0, q);
            q = Math.min(h - 1, q);
            if (border != null) {
                border[p][q] = true;
            }
        }
    }

    /**
     * @param border marks {@code true} if the position was drawn something on
     * @see H#curveTo(Pixmap, Vector2[], Color, int, boolean[][])
     */
    public static void drawCurve(Pixmap pixmap, Vector2[] points, Color stroke, int strokeWidth, boolean[][] border) {
        if (strokeWidth == 0) {
            curveTo(pixmap, points, stroke, strokeWidth, border);
        } else {
            int w = pixmap.getWidth(), h = pixmap.getHeight(), color;
            Pixmap tmp = new Pixmap(w, h, pixmap.getFormat());
            curveTo(tmp, points, stroke, strokeWidth, border);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    color = tmp.getPixel(i, j);
                    if (color != Color.rgba8888(Color.CLEAR)) {
                        border[i][j] = true;
                    }
                    pixmap.drawPixel(i, j, color);
                }
            }
            tmp.dispose();
        }
    }

    // @formatter:off
    // @off
    public static int[][] c = {
        /*y  0  1  2  3  4 */// x
            {1, 0, 0, 0, 0}, // 0
            {1, 1, 0, 0, 0}, // 1
            {1, 2, 1, 0, 0}, // 2
            {1, 3, 3, 1, 0}, // 3
            {1, 4, 6, 4, 1}  // 4
    };
    // @formatter:on
    // @on

    public static int C(int x, int y) {
        if (y == 0 || y == x) return 1;
        if (y < 0 || y > x) return 0; // risk!
        return C(x - 1, y - 1) + C(x - 1, y);
    }

    public static int[] dx = {1, 0, -1, 0},
            dy = {0, 1, 0, -1};

    public static void fillColor(Pixmap pixmap, boolean[][] border, Color fill) {
        if (fill == null || fill.equals(Color.CLEAR)) return;
        int w = pixmap.getWidth(), h = pixmap.getHeight();
        Queue<GridPoint2> q = new Queue<>();
        Array<GridPoint2> list = new Array<>(w * h);

        boolean[][] vis = new boolean[border.length][border[0].length];
        GridPoint2 p;
        int color = Color.rgba8888(fill);

        int x, y;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (vis[i][j] || border[i][j]) continue;
                boolean isOverflow = false;
                q.addLast(new GridPoint2(i, j));
                while (q.notEmpty()) {
                    p = q.removeFirst();
                    if (vis[p.x][p.y]) {
                        continue;
                    }
                    vis[p.x][p.y] = true;
                    if (border[p.x][p.y]) {
                        continue;
                    }

                    for (int t = 0; t < dx.length; t++) {
                        x = p.x + dx[t];
                        y = p.y + dy[t];
                        if (x < 0 || x >= w || y < 0 || y >= h) {
                            // not border but touch the edge of the pixmap, should be outside the border
                            isOverflow = true;
                            continue;
                        }
                        if (!vis[x][y]) {
                            if ((!border[x][y])) {
                                q.addLast(new GridPoint2(x, y));
                            }
                        }
                    }
                    if (!border[p.x][p.y]) {
                        list.add(p);
                    }
                }
                if (!isOverflow) {
                    for (GridPoint2 point : list) {
                        pixmap.drawPixel(point.x, point.y, color);
                    }
                }
                list.clear();
            }
        }
    }

    public static double svgReadDouble(String s, double k) {
        if (s.endsWith("%"))
            return Double.parseDouble(s.substring(0, s.length() - 2)) * k / 100.0;
        return Double.parseDouble(s);
    }

    @Deprecated
    public static Color svgReadColor(String color, Color currentColor) {
        // TODO implement
        if (color.equals("currentColor")) return currentColor;
        if (color.startsWith("#")) return Color.valueOf(color.replace("#", ""));
        return colorMap.get(color);
    }

    public static Color svgReadColor(XmlReader.Element element, String property) {
        try {
            String color = getAttribute(element, property);
            if (color.toLowerCase().equals("currentcolor")) {
                do {
                    color = getAttribute(element, "color");
                    element = element.getParent();
                } while (color.toLowerCase().equals("currentcolor"));
            }
            if (color.equals("none")) {
                return Color.CLEAR;
            }
            if (color.startsWith("#")) return Color.valueOf(color.replace("#", ""));
            return colorMap.get(color);
        } catch (NullPointerException | GdxRuntimeException ignored) {
            return defaultColor;
        }
    }

    public static Color color8BitsToFloat(int r, int g, int b, int a) {
        return new Color(r / 256.0f, g / 256.0f, b / 256.0f, a / 256.0f);
    }

    public static Color color8BitsToFloat(int r, int g, int b) {
        return new Color(r / 256.0f, g / 256.0f, b / 256.0f, 1f);
    }

    public static String getAttribute(XmlReader.Element element, String attribute) {
        if (element == null) {
            throw new GdxRuntimeException("cannot read attribute `" + attribute + "` on null element");
        }
        try {
            return element.getAttribute(attribute);
        } catch (GdxRuntimeException | NullPointerException exception) {
            try {
                return getAttribute(element.getParent(), attribute);
            } catch (GdxRuntimeException | NullPointerException e) {
                throw new GdxRuntimeException("No attribute \"" + attribute + "\" in element \"" + element.toString() + "\" or its parents");
            }
        }
    }

    public static class SVGBasicInfo {
        int x_min, y_min;
        int width, height;
        Color fill, stroke;
        double strokeWidth;

        public SVGBasicInfo(XmlReader.Element element) {
            try {
                String[] viewbox = H.getAttribute(element, "viewBox").split(" ");
                x_min = Integer.parseInt(viewbox[0]);
                y_min = Integer.parseInt(viewbox[1]);
                width = Integer.parseInt(viewbox[2]);
                height = Integer.parseInt(viewbox[3]);
            } catch (GdxRuntimeException | NullPointerException ignored) {
                width = Integer.parseInt(H.getAttribute(element, "width"));
                height = Integer.parseInt(H.getAttribute(element, "height"));
            }
            fill = H.svgReadColor(element, "fill");
            stroke = H.svgReadColor(element, "stroke");
            strokeWidth = H.svgReadDouble(H.getAttribute(element, "stroke-width"), Math.sqrt(width * width + height * height));
        }
    }

}
