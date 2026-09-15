package particlesim.movement;

import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Saccadic motion: the target holds still, then suddenly jumps to a new random
 * location at randomized intervals (between 1.5 and 3 seconds). This abrupt
 * repositioning is used to probe reaction time to sudden gaze shifts.
 *
 * Inspired by saccadic eye-movement tests (in spirit, tests like King-Devick
 * that involve rapid gaze shifts). This is a simplified model for a course
 * project, not a clinical instrument.
 *
 * Implementation note: {@link #getPosition} is called repeatedly with a
 * monotonically increasing time by the animation loop. We lazily generate the
 * next jump whenever the elapsed time passes the scheduled jump time, so the
 * held position stays constant between jumps (that flat hold is exactly what
 * makes reaction latency measurable afterwards).
 */
public class SaccadicPath implements MovementStrategy {

    private static final double MIN_INTERVAL = 1.5; // seconds between jumps
    private static final double MAX_INTERVAL = 3.0;
    private static final double MARGIN = 40;        // keep targets off the edges

    private final int width;
    private final int height;
    private final Random random = new Random();

    private Point2D currentPosition;
    private double nextJumpTime;

    public SaccadicPath(int width, int height) {
        this.width = width;
        this.height = height;
        // Start centered, then schedule the first jump.
        this.currentPosition = new Point2D.Double(width / 2.0, height / 2.0);
        this.nextJumpTime = randomInterval();
    }

    @Override
    public Point2D getPosition(double elapsedSeconds) {
        // If we've reached (or passed) one or more scheduled jump times, jump.
        // A while-loop handles the rare case of skipping past several at once.
        while (elapsedSeconds >= nextJumpTime) {
            currentPosition = randomPoint();
            nextJumpTime += randomInterval();
        }
        return currentPosition;
    }

    private Point2D randomPoint() {
        double x = MARGIN + random.nextDouble() * (width - 2 * MARGIN);
        double y = MARGIN + random.nextDouble() * (height - 2 * MARGIN);
        return new Point2D.Double(x, y);
    }

    private double randomInterval() {
        return MIN_INTERVAL + random.nextDouble() * (MAX_INTERVAL - MIN_INTERVAL);
    }

    @Override
    public String getName() {
        return "Saccadic";
    }
}
