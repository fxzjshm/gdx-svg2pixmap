package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.StringBuilder;

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
                double x2 = Double.valueOf(params.get(0)) / width * pixmap.getWidth(), y2 = Double.valueOf(params.get(1)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2((float) x2, (float) y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = (float) x2;
                currentPosition.y = (float) y2;
            }
            if (newCommand == 'H') {
                double x2 = Double.valueOf(params.get(0)) / width * pixmap.getWidth();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2((float) x2, currentPosition.y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = (float) x2;
            }
            if (newCommand == 'V') {
                double y2 = Double.valueOf(params.get(0)) / height * pixmap.getHeight();
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2(currentPosition.x, (float) y2)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.y = (float) y2;
            }
            if (newCommand == 'C') {
                double x1 = Double.valueOf(params.get(0)) / width * pixmap.getWidth(), y1 = Double.valueOf(params.get(1)) / height * pixmap.getHeight();
                double x2 = Double.valueOf(params.get(2)) / width * pixmap.getWidth(), y2 = Double.valueOf(params.get(3)) / height * pixmap.getHeight();
                double x = Double.valueOf(params.get(4)) / width * pixmap.getWidth(), y = Double.valueOf(params.get(5)) / height * pixmap.getHeight();
                lastCPoint = new Vector2((float) x2, (float) y2);
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2((float) x1, (float) y1), lastCPoint, new Vector2((float) x, (float) y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = (float) x;
                currentPosition.y = (float) y;
            }
            if (newCommand == 'S') {
                double x2 = Double.valueOf(params.get(0)) / width * pixmap.getWidth(), y2 = Double.valueOf(params.get(1)) / height * pixmap.getHeight();
                double x = Double.valueOf(params.get(2)) / width * pixmap.getWidth(), y = Double.valueOf(params.get(3)) / height * pixmap.getHeight();
                double x1, y1;
                if (lastCPoint != null) {
                    x1 = 2 * currentPosition.x - lastCPoint.x;
                    y1 = 2 * currentPosition.y - lastCPoint.y;
                } else {
                    x1 = x2;
                    y1 = y2;
                }
                lastCPoint = new Vector2((float) x2, (float) y2);
                H.curveTo(pixmap, new Vector2[]{currentPosition, new Vector2((float) x1, (float) y1), lastCPoint, new Vector2((float) x, (float) y)}, stroke, (int) Math.round(strokeWidth));
            }
            if (newCommand == 'Q') {
                double x1 = Double.valueOf(params.get(0)) / width * pixmap.getWidth(), y1 = Double.valueOf(params.get(1)) / height * pixmap.getHeight();
                double x = Double.valueOf(params.get(2)) / width * pixmap.getWidth(), y = Double.valueOf(params.get(3)) / height * pixmap.getHeight();
                lastQPoint = new Vector2((float) x1, (float) y1);
                H.curveTo(pixmap, new Vector2[]{currentPosition, lastQPoint, new Vector2((float) x, (float) y)}, stroke, (int) Math.round(strokeWidth));

                currentPosition.x = (float) x;
                currentPosition.y = (float) y;
            }
            if (newCommand == 'T') {
                double x = Double.valueOf(params.get(0)) / width * pixmap.getWidth(), y = Double.valueOf(params.get(1)) / height * pixmap.getHeight();
                double x1, y1;
                if (lastCPoint != null) {
                    x1 = 2 * currentPosition.x - lastQPoint.x;
                    y1 = 2 * currentPosition.y - lastQPoint.y;
                } else {
                    x1 = x;
                    y1 = y;
                }
                lastQPoint = new Vector2((float) x1, (float) y1);
                H.curveTo(pixmap, new Vector2[]{currentPosition, lastCPoint, new Vector2((float) x, (float) y)}, stroke, (int) Math.round(strokeWidth));
            }

            // Clear useless control points
            if (newCommand != 'Q') lastQPoint = null;
            if (newCommand != 'C') lastCPoint = null;

            params.clear();
        }
        return pixmap;
    }

    /**
     * Helpful(-less) methods.
     */
    public static class H {

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
                else sb.append(c);
                if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')) sb.append(' ');
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
         */
        public static void r2a(char command, List<String> params, Vector2 currentPoint) {
            boolean isX = true;
            if (command == 'a') {
                params.set(0, String.valueOf(Double.valueOf(params.get(0) + currentPoint.x)));
                params.set(1, String.valueOf(Double.valueOf(params.get(1) + currentPoint.y)));
                params.set(5, String.valueOf(Double.valueOf(params.get(5) + currentPoint.x)));
                params.set(6, String.valueOf(Double.valueOf(params.get(6) + currentPoint.y)));
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
    }
}