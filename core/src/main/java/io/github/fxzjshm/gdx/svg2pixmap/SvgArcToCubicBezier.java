package io.github.fxzjshm.gdx.svg2pixmap;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Translated from https://github.com/colinmeinke/svg-arc-to-cubic-bezier/blob/master/src/index.js
 */
public class SvgArcToCubicBezier {

    public static final double TAU = Math.PI * 2;

    static Vector2 mapToEllipse(Vector2 vec, double rx, double ry, double cosphi, double sinphi, double centerx, double centery) {
        double x = vec.x * rx;
        double y = vec.y * ry;

        final double xp = cosphi * x - sinphi * y;
        final double yp = sinphi * x + cosphi * y;

        return new Vector2((float) (xp + centerx), (float) (yp + centery));
    }

    static Vector2[] approxUnitArc(double ang1, double ang2) {
        // If 90 degree circular arc, use a finalant
        // as derived from http://spencermortensen.com/articles/bezier-circle
        final double a = ((ang2 == 1.5707963267948966)
                ? 0.551915024494
                : (ang2 == -1.5707963267948966
                ? -0.551915024494
                : 4.0 / 3 * Math.tan(ang2 / 4)));

        final double x1 = Math.cos(ang1);
        final double y1 = Math.sin(ang1);
        final double x2 = Math.cos(ang1 + ang2);
        final double y2 = Math.sin(ang1 + ang2);

        return new Vector2[]{
                new Vector2((float) (x1 - y1 * a), (float) (y1 + x1 * a)),
                new Vector2((float) (x2 + y2 * a), (float) (y2 - x2 * a)),
                new Vector2((float) (x2), (float) (y2))
        };
    }

    static double vectorAngle(double ux, double uy, double vx, double vy) {
        final double sign = (ux * vy - uy * vx < 0) ? -1 : 1;

        double dot = ux * vx + uy * vy;

        if (dot > 1) {
            dot = 1;
        }

        if (dot < -1) {
            dot = -1;
        }

        return (sign * Math.acos(dot));
    }

    static double[] getArcCenter(double px, double py, double cx, double cy, double rx, double ry, int largeArcFlag, int sweepFlag, double sinphi, double cosphi, double pxp, double pyp) {
        final double rxsq = Math.pow(rx, 2);
        final double rysq = Math.pow(ry, 2);
        final double pxpsq = Math.pow(pxp, 2);
        final double pypsq = Math.pow(pyp, 2);

        double radicant = (rxsq * rysq) - (rxsq * pypsq) - (rysq * pxpsq);

        if (radicant < 0) {
            radicant = 0;
        }

        radicant /= (rxsq * pypsq) + (rysq * pxpsq);
        radicant = Math.sqrt(radicant) * (largeArcFlag == sweepFlag ? -1 : 1);

        final double centerxp = radicant * rx / ry * pyp;
        final double centeryp = radicant * -ry / rx * pxp;

        final double centerx = cosphi * centerxp - sinphi * centeryp + (px + cx) / 2;
        final double centery = sinphi * centerxp + cosphi * centeryp + (py + cy) / 2;

        final double vx1 = (pxp - centerxp) / rx;
        final double vy1 = (pyp - centeryp) / ry;
        final double vx2 = (-pxp - centerxp) / rx;
        final double vy2 = (-pyp - centeryp) / ry;
        double ang1 = vectorAngle(1, 0, vx1, vy1);
        double ang2 = vectorAngle(vx1, vy1, vx2, vy2);

        if (sweepFlag == 0 && ang2 > 0) {
            ang2 -= TAU;
        }

        if (sweepFlag == 1 && ang2 < 0) {
            ang2 += TAU;
        }

        return new double[]{centerx, centery, ang1, ang2};
    }

    public static List<Vector2[]> arcToBezier(double px, double py, double cx, double cy, double rx, double ry, double xAxisRotation, int largeArcFlag, int sweepFlag) {
        if (rx == 0 || ry == 0) {
            return new ArrayList<>(0);
        }

        final ArrayList<Vector2[]> curves = new ArrayList<>(3);

        final double sinphi = Math.sin(xAxisRotation * TAU / 360);
        final double cosphi = Math.cos(xAxisRotation * TAU / 360);

        final double pxp = cosphi * (px - cx) / 2 + sinphi * (py - cy) / 2;
        final double pyp = -sinphi * (px - cx) / 2 + cosphi * (py - cy) / 2;

        if (pxp == 0 && pyp == 0) {
            return new ArrayList<>(0);
        }

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        final double lambda = Math.pow(pxp, 2) / Math.pow(rx, 2) + Math.pow(pyp, 2) / Math.pow(ry, 2);

        if (lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }

        double[] results = getArcCenter(px, py, cx, cy, rx, ry, largeArcFlag, sweepFlag, sinphi, cosphi, pxp, pyp);
        double centerx = results[0], centery = results[1], ang1 = results[2], ang2 = results[3];

        // If 'ang2' == 90.0000000001, then `ratio` will evaluate to
        // 1.0000000001. This causes `segments` to be greater than one, which is an
        // unecessary split, and adds extra points to the bezier curve. To alleviate
        // this issue, we round to 1.0 when the ratio is close to 1.0.
        double ratio = Math.abs(ang2) / (TAU / 4);
        if (Math.abs(1.0 - ratio) < 0.0000001) {
            ratio = 1.0;
        }

        int segments = (int) Math.max(Math.ceil(ratio), 1);

        ang2 /= segments;

        for (int i = 0; i < segments; i++) {
            curves.add(approxUnitArc(ang1, ang2));
            ang1 += ang2;
        }

        final double finalRx = rx;
        final double finalRy = ry;
        return curves.stream().map(curve -> {
            Vector2 vec1 = mapToEllipse(curve[0], finalRx, finalRy, cosphi, sinphi, centerx, centery);
            Vector2 vec2 = mapToEllipse(curve[1], finalRx, finalRy, cosphi, sinphi, centerx, centery);
            Vector2 vec = mapToEllipse(curve[2], finalRx, finalRy, cosphi, sinphi, centerx, centery);
            return new Vector2[]{vec1, vec2, vec};
        }).distinct().collect(Collectors.toList());
    }
}
