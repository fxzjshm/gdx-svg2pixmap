package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlReader;

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

    public static Pixmap svg2Pixmap(String fileContent) {
        XmlReader reader = new XmlReader();
        XmlReader.Element root = reader.parse(fileContent);
        int width = Integer.valueOf(root.getAttribute("width"));
        int height = Integer.valueOf(root.getAttribute("height"));
        Color fill = null, stroke = null;
        double strokeWidth = -1;
        try {
            fill = Color.valueOf(root.getAttribute("fill")); //TODO parse color
        } catch (RuntimeException ignored) {
        }
        try {
            stroke = Color.valueOf(root.getAttribute("stroke")); //TODO parse color
        } catch (RuntimeException ignored) {
        }
        try {
            strokeWidth = H.svgReadDouble(root.getAttribute("strokeWidth"), Math.sqrt(width * width + height * height));
        } catch (RuntimeException ignored) {
        }

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        XmlReader.Element[] children = root.getChildrenByName("path").toArray(XmlReader.Element.class);
        for (XmlReader.Element child : children) {
            try {
                if (child.getName().equals("path"))
                    path2Pixmap(width, height, child.getAttribute("d"), fill == null ? Color.valueOf(child.getAttribute("fill")) : fill, stroke == null ? Color.valueOf(child.getAttribute("stroke")) : stroke, strokeWidth == -1 ? H.svgReadDouble(root.getAttribute("strokeWidth"), Math.sqrt(width * width + height * height)) : strokeWidth, pixmap);
            }catch (Exception ignored){ //TODO Dangerous here !!!
            }
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