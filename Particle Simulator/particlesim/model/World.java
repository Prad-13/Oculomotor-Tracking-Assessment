package particlesim.model;

/**
 * Holds the state shared by the test: the arena size (kept in sync with the
 * panel) and the single moving {@link Target}.
 *
 * Simplified from the original particle-simulator version, which held a whole
 * list of particles plus gravity settings. Gravity and multi-particle state are
 * gone; a tracking test has exactly one controlled target and no forces.
 */
public class World {

    private int width;
    private int height;

    private final Target target;

    public World(int width, int height, Target target) {
        this.width = width;
        this.height = height;
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** Called when the panel is resized so movement paths can be sized to it. */
    public void setBounds(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
