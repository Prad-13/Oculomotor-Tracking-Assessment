package particlesim.analysis;

/**
 * One recorded sample taken during a test: where the mouse was and where the
 * target was at a given moment. A whole test session is just a list of these.
 *
 * A record is a perfect fit here - it is immutable data with no behavior.
 *
 * @param timeSeconds seconds since the test started (not wall-clock time)
 * @param mouseX      mouse cursor x, in panel pixels
 * @param mouseY      mouse cursor y, in panel pixels
 * @param targetX     target x, in panel pixels
 * @param targetY     target y, in panel pixels
 */
public record TrackPoint(double timeSeconds,
                         double mouseX, double mouseY,
                         double targetX, double targetY) {

    /** Straight-line distance between the mouse and the target for this sample. */
    public double deviation() {
        double dx = mouseX - targetX;
        double dy = mouseY - targetY;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
