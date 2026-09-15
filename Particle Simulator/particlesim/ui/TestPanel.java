package particlesim.ui;

import particlesim.analysis.Tracker;
import particlesim.model.Target;
import particlesim.model.World;
import particlesim.movement.TestType;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The test surface (renamed from the original {@code SimPanel}).
 *
 * Responsibilities are timing + presentation only:
 *   - run a ~60fps Swing Timer that advances the target along its movement path,
 *     feeds the current mouse/target positions to the {@link Tracker}, and
 *     repaints;
 *   - draw the moving target, the user's mouse cursor (a crosshair), and a short
 *     fading trail of recent target positions;
 *   - run a 3-2-1 countdown before each test and auto-end after a fixed duration.
 *
 * It contains no movement math and no metric math - those live in the
 * {@code movement} and {@code analysis} packages. When a run finishes it simply
 * hands the recorded data up to a listener (wired in Main).
 */
public class TestPanel extends JPanel {

    private static final int FPS_DELAY_MS = 16;      // ~60 fps
    private static final double TEST_DURATION = 20;  // seconds
    private static final int COUNTDOWN_SECONDS = 3;
    private static final int TRAIL_LENGTH = 25;      // trail sample count

    /** Simple test lifecycle. */
    private enum State { IDLE, COUNTDOWN, RUNNING, FINISHED }

    /** Callback used to hand results up to the app when a run completes. */
    public interface CompletionListener {
        void onComplete(java.util.List<particlesim.analysis.TrackPoint> data,
                        boolean saccadic);
    }

    private final World world;
    private final Tracker tracker;
    private final Timer timer;

    private State state = State.IDLE;
    private TestType currentType;
    private CompletionListener completionListener;

    // Nanosecond timestamp marking the start of the current phase.
    private long phaseStartNanos;

    // Latest known mouse position inside the panel, kept fresh by the motion
    // listener so each frame can record it against the target position.
    private double mouseX;
    private double mouseY;

    // Recent target positions for the fading trail (newest last).
    private final Deque<Point2D> trail = new ArrayDeque<>();

    public TestPanel(World world, Tracker tracker) {
        this.world = world;
        this.tracker = tracker;

        setBackground(Color.BLACK);

        // Keep world bounds in sync so movement paths match the visible area.
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                world.setBounds(getWidth(), getHeight());
            }
        });

        // Track the mouse position (both while moving and while dragging).
        MouseMotionAdapter motion = new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        };
        addMouseMotionListener(motion);

        timer = new Timer(FPS_DELAY_MS, e -> tick());
        timer.start();
    }

    public void setCompletionListener(CompletionListener listener) {
        this.completionListener = listener;
    }

    /**
     * Begin a new test of the given type: install the matching movement
     * strategy, arm the tracker, and start the 3-2-1 countdown.
     */
    public void startTest(TestType type) {
        this.currentType = type;
        Target target = world.getTarget();
        target.setStrategy(type.createStrategy(world.getWidth(), world.getHeight()));
        target.update(0); // place it at its t=0 position immediately

        trail.clear();
        mouseX = world.getWidth() / 2.0;
        mouseY = world.getHeight() / 2.0;

        state = State.COUNTDOWN;
        phaseStartNanos = System.nanoTime();
    }

    /** Return to the idle state, clearing any in-progress run. */
    public void reset() {
        state = State.IDLE;
        tracker.stop();
        trail.clear();
    }

    /** One animation frame: advance state, update the target, record, repaint. */
    private void tick() {
        double phaseElapsed = (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0;

        switch (state) {
            case COUNTDOWN -> {
                if (phaseElapsed >= COUNTDOWN_SECONDS) {
                    // Countdown over: begin the actual recorded run.
                    state = State.RUNNING;
                    phaseStartNanos = System.nanoTime();
                    tracker.start();
                }
            }
            case RUNNING -> {
                Target target = world.getTarget();
                target.update(phaseElapsed);
                pushTrail(target.getX(), target.getY());

                tracker.record(phaseElapsed, mouseX, mouseY,
                        target.getX(), target.getY());

                if (phaseElapsed >= TEST_DURATION) {
                    finishRun();
                }
            }
            default -> { /* IDLE / FINISHED: nothing to advance */ }
        }

        repaint();
    }

    private void finishRun() {
        state = State.FINISHED;
        tracker.stop();
        if (completionListener != null) {
            completionListener.onComplete(tracker.getPoints(),
                    currentType != null && currentType.isSaccadic());
        }
    }

    private void pushTrail(double x, double y) {
        trail.addLast(new Point2D.Double(x, y));
        while (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
    }

    // ---------------------------------------------------------------- drawing

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (state == State.COUNTDOWN) {
            drawCountdown(g2);
            return;
        }

        if (state == State.RUNNING) {
            drawTrail(g2);
            drawTarget(g2);
            drawMouseCrosshair(g2);
            drawTimeRemaining(g2);
        } else {
            drawIdleHint(g2);
        }
    }

    private void drawTrail(Graphics2D g2) {
        int i = 0;
        int n = trail.size();
        for (Point2D p : trail) {
            // Older points (front of the deque) are more transparent.
            float alpha = (float) (i + 1) / n * 0.5f;
            g2.setColor(new Color(0.2f, 0.6f, 1f, alpha));
            int r = 4;
            g2.fillOval((int) (p.getX() - r), (int) (p.getY() - r), r * 2, r * 2);
            i++;
        }
    }

    private void drawTarget(Graphics2D g2) {
        Target t = world.getTarget();
        double r = t.getRadius();
        g2.setColor(new Color(80, 170, 255));
        g2.fillOval((int) (t.getX() - r), (int) (t.getY() - r),
                (int) (r * 2), (int) (r * 2));
        // A white ring makes the exact center easy to fixate on.
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval((int) (t.getX() - r), (int) (t.getY() - r),
                (int) (r * 2), (int) (r * 2));
    }

    private void drawMouseCrosshair(Graphics2D g2) {
        g2.setColor(new Color(255, 90, 90));
        g2.setStroke(new BasicStroke(1.5f));
        int size = 10;
        int mx = (int) mouseX;
        int my = (int) mouseY;
        g2.drawLine(mx - size, my, mx + size, my);
        g2.drawLine(mx, my - size, mx, my + size);
    }

    private void drawCountdown(Graphics2D g2) {
        double phaseElapsed = (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0;
        int remaining = COUNTDOWN_SECONDS - (int) phaseElapsed;
        if (remaining < 1) {
            remaining = 1;
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 96));
        drawCentered(g2, String.valueOf(remaining), getHeight() / 2);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g2, "Get ready - follow the target with your mouse",
                getHeight() / 2 + 70);
    }

    private void drawTimeRemaining(Graphics2D g2) {
        double phaseElapsed = (System.nanoTime() - phaseStartNanos) / 1_000_000_000.0;
        int remaining = (int) Math.ceil(TEST_DURATION - phaseElapsed);
        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("Time left: " + remaining + "s", 12, 22);
    }

    private void drawIdleHint(Graphics2D g2) {
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g2, "Pick a test type below and press Start", getHeight() / 2);
    }

    /** Draw a string horizontally centered at the given y baseline. */
    private void drawCentered(Graphics2D g2, String text, int y) {
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (getWidth() - textWidth) / 2, y);
    }
}
