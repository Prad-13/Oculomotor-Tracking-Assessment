package particlesim.movement;

import java.awt.geom.Point2D;

/**
 * Smooth-pursuit motion: a continuous, predictable figure-eight (Lissajous)
 * path. Because the position is a smooth function of time, the target never
 * jumps, which is what lets us probe smooth visual tracking.
 *
 * Inspired by the smooth-pursuit portion of clinical eye-movement exams. This
 * is a simplified model for a course project, not a clinical instrument.
 *
 * The path is a Lissajous curve with a 1:2 frequency ratio, which traces a
 * horizontal figure-eight:
 *     x(t) = cx + ax * sin(w * t)
 *     y(t) = cy + ay * sin(2 * w * t)
 */
public class SmoothPursuitPath implements MovementStrategy {

    private final double centerX;
    private final double centerY;
    private final double ampX;
    private final double ampY;
    private final double omega; // angular speed (radians per second)

    /**
     * @param width  panel width in pixels
     * @param height panel height in pixels
     */
    public SmoothPursuitPath(int width, int height) {
        this.centerX = width / 2.0;
        this.centerY = height / 2.0;
        // Use 35% of each dimension so the target stays comfortably on screen.
        this.ampX = width * 0.35;
        this.ampY = height * 0.35;
        // One full figure-eight cycle every 8 seconds.
        double periodSeconds = 8.0;
        this.omega = 2 * Math.PI / periodSeconds;
    }

    @Override
    public Point2D getPosition(double elapsedSeconds) {
        double x = centerX + ampX * Math.sin(omega * elapsedSeconds);
        double y = centerY + ampY * Math.sin(2 * omega * elapsedSeconds);
        return new Point2D.Double(x, y);
    }

    @Override
    public String getName() {
        return "Smooth Pursuit";
    }
}
