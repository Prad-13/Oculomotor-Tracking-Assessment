package particlesim.movement;

import java.awt.geom.Point2D;

/**
 * Strategy interface describing how the on-screen target moves over time.
 *
 * Each implementation defines one kind of motion path used by the tracking
 * test. The panel simply asks "where should the target be at time t?" and the
 * strategy answers, so the movement logic stays completely out of the UI.
 *
 * (This package replaces the old {@code physics} package. The original project
 * simulated free-moving particles with gravity and collisions; here the target
 * follows a controlled, predictable path instead, so that motion is repeatable
 * across test runs.)
 */
public interface MovementStrategy {

    /**
     * @param elapsedSeconds seconds since the test started
     * @return the target's position at that moment, in panel pixel coordinates
     */
    Point2D getPosition(double elapsedSeconds);

    /** Human-readable name, used for labels and comments. */
    String getName();
}
