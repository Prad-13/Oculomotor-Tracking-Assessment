package particlesim.movement;

/**
 * The two kinds of tracking test the user can choose from. Keeping the choice
 * here (rather than in the UI) lets the movement package own the mapping from
 * "test type" to the concrete {@link MovementStrategy} that drives the target.
 */
public enum TestType {

    SMOOTH_PURSUIT("Smooth Pursuit"),
    SACCADIC("Saccadic");

    private final String label;

    TestType(String label) {
        this.label = label;
    }

    /** Build the movement strategy for this test, sized to the panel. */
    public MovementStrategy createStrategy(int width, int height) {
        return switch (this) {
            case SMOOTH_PURSUIT -> new SmoothPursuitPath(width, height);
            case SACCADIC -> new SaccadicPath(width, height);
        };
    }

    public boolean isSaccadic() {
        return this == SACCADIC;
    }

    @Override
    public String toString() {
        return label; // so the JComboBox shows a friendly name
    }
}
