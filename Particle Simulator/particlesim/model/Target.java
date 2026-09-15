package particlesim.model;

import particlesim.movement.MovementStrategy;

import java.awt.geom.Point2D;

/**
 * The single moving target the user tries to track with the mouse.
 *
 * Repurposed from the original {@code Particle} class. The old physics fields
 * (velocity vx/vy, mass, and the physics use of radius) are gone: the target no
 * longer obeys forces or collisions. Instead its position is driven entirely by
 * a {@link MovementStrategy}, and the only physical attribute left is
 * {@code radius}, kept purely for how big to draw it.
 */
public class Target {

    // Current position, in panel pixels. Updated each frame from the strategy.
    private double x;
    private double y;

    // Drawing radius only (no physical meaning anymore).
    private final double radius;

    // The motion path currently driving this target. Swapped when the user
    // picks a different test type.
    private MovementStrategy strategy;

    public Target(double radius) {
        this.radius = radius;
    }

    /** Recompute the position for the given elapsed time using the strategy. */
    public void update(double elapsedSeconds) {
        if (strategy == null) {
            return;
        }
        Point2D p = strategy.getPosition(elapsedSeconds);
        this.x = p.getX();
        this.y = p.getY();
    }

    public void setStrategy(MovementStrategy strategy) {
        this.strategy = strategy;
    }

    public MovementStrategy getStrategy() {
        return strategy;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return radius;
    }
}
