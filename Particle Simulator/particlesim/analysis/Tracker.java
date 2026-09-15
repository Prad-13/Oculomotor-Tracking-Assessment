package particlesim.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Collects {@link TrackPoint} samples during a test run.
 *
 * The panel drives this: on every animation frame while a test is running it
 * calls {@link #record}, passing the latest mouse position (kept up to date by
 * a MouseMotionListener on the panel) and the target's current position. The
 * tracker itself holds no timing loop and no UI - it is purely the data
 * recorder, which keeps recording separate from both movement and rendering.
 */
public class Tracker {

    private final List<TrackPoint> points = new ArrayList<>();
    private boolean recording = false;

    /** Begin a fresh recording session, discarding any previous samples. */
    public void start() {
        points.clear();
        recording = true;
    }

    /** Stop recording; collected samples remain available via {@link #getPoints}. */
    public void stop() {
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }

    /** Record one sample. Ignored if we are not currently recording. */
    public void record(double timeSeconds,
                       double mouseX, double mouseY,
                       double targetX, double targetY) {
        if (!recording) {
            return;
        }
        points.add(new TrackPoint(timeSeconds, mouseX, mouseY, targetX, targetY));
    }

    /** An unmodifiable view of everything recorded so far. */
    public List<TrackPoint> getPoints() {
        return Collections.unmodifiableList(points);
    }
}
