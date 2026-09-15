package particlesim.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes summary metrics from a completed test session (a list of
 * {@link TrackPoint}s). This runs once, after the test ends - it does no live
 * tracking and touches no UI, keeping the number-crunching in one place.
 *
 * Metrics produced:
 *   - average pixel deviation between mouse and target over the whole session;
 *   - a deviation-over-time series (for the results graph);
 *   - for saccadic sessions, the reaction latency for each detected jump.
 */
public class MetricsEngine {

    // --- Tunable heuristics (documented so a reader can adjust them) ---------

    // A target "jump" is detected when the target moves more than this many
    // pixels between two consecutive frames. Smooth-pursuit motion moves only a
    // few pixels per frame, while a saccadic jump moves hundreds, so this cleanly
    // separates the two without the engine needing to know the strategy type.
    private static final double JUMP_DETECT_PX = 50;

    // The mouse is considered to have "started moving" toward a new target once
    // its speed exceeds this many pixels per second. This is the velocity
    // threshold heuristic used to estimate reaction latency; it is deliberately
    // above normal hand jitter so a resting hand does not trigger it.
    private static final double MOVE_ONSET_PX_PER_SEC = 150;

    /**
     * Immutable bundle of the computed results, handed to the ResultsPanel.
     */
    public static class Result {
        public final boolean saccadic;
        public final double averageDeviationPx;
        /** Largest single-frame deviation seen during the session. */
        public final double peakDeviationPx;
        /** Number of samples recorded during the session. */
        public final int sampleCount;
        /** One latency (in ms) per detected jump; empty for smooth pursuit. */
        public final List<Double> reactionLatenciesMs;
        /** Mean of {@link #reactionLatenciesMs}, or NaN if there were none. */
        public final double averageReactionLatencyMs;

        Result(boolean saccadic, double averageDeviationPx, double peakDeviationPx,
               int sampleCount,
               List<Double> reactionLatenciesMs,
               double averageReactionLatencyMs) {
            this.saccadic = saccadic;
            this.averageDeviationPx = averageDeviationPx;
            this.peakDeviationPx = peakDeviationPx;
            this.sampleCount = sampleCount;
            this.reactionLatenciesMs = reactionLatenciesMs;
            this.averageReactionLatencyMs = averageReactionLatencyMs;
        }

        /** Fastest (minimum) reaction latency in ms, or NaN if no jumps. */
        public double getMinLatencyMs() {
            double min = Double.NaN;
            for (double v : reactionLatenciesMs) {
                if (Double.isNaN(min) || v < min) {
                    min = v;
                }
            }
            return min;
        }

        /** Slowest (maximum) reaction latency in ms, or NaN if no jumps. */
        public double getMaxLatencyMs() {
            double max = Double.NaN;
            for (double v : reactionLatenciesMs) {
                if (Double.isNaN(max) || v > max) {
                    max = v;
                }
            }
            return max;
        }
    }

    /**
     * Analyze a finished session.
     *
     * @param points   the recorded samples, in time order
     * @param saccadic whether this was a saccadic test (enables latency metrics)
     */
    public Result analyze(List<TrackPoint> points, boolean saccadic) {
        double avgDeviation = 0;
        double peakDeviation = 0;
        if (!points.isEmpty()) {
            double sum = 0;
            for (TrackPoint p : points) {
                double d = p.deviation();
                sum += d;
                peakDeviation = Math.max(peakDeviation, d);
            }
            avgDeviation = sum / points.size();
        }

        List<Double> latencies = saccadic
                ? computeReactionLatencies(points)
                : new ArrayList<>();
        double avgLatency = average(latencies);

        return new Result(saccadic, avgDeviation, peakDeviation, points.size(),
                latencies, avgLatency);
    }

    /**
     * For each detected target jump, estimate how long it took the mouse to
     * start moving toward the new target position.
     *
     * Algorithm:
     *   1. Walk the samples; a jump is a frame where the target moved more than
     *      JUMP_DETECT_PX from the previous frame.
     *   2. From the jump onward, find the first sample where the mouse's speed
     *      exceeds MOVE_ONSET_PX_PER_SEC *and* it is moving toward the new
     *      target (positive dot product with the direction to the target).
     *   3. Latency = that sample's time minus the jump time, in milliseconds.
     */
    private List<Double> computeReactionLatencies(List<TrackPoint> points) {
        List<Double> latencies = new ArrayList<>();

        for (int i = 1; i < points.size(); i++) {
            TrackPoint prev = points.get(i - 1);
            TrackPoint jump = points.get(i);

            double targetMove = distance(
                    prev.targetX(), prev.targetY(),
                    jump.targetX(), jump.targetY());
            if (targetMove < JUMP_DETECT_PX) {
                continue; // no jump here
            }

            // Search forward from the jump for the mouse-movement onset.
            for (int j = i + 1; j < points.size(); j++) {
                TrackPoint a = points.get(j - 1);
                TrackPoint b = points.get(j);

                double dt = b.timeSeconds() - a.timeSeconds();
                if (dt <= 0) {
                    continue;
                }

                double mouseDx = b.mouseX() - a.mouseX();
                double mouseDy = b.mouseY() - a.mouseY();
                double mouseSpeed = Math.sqrt(mouseDx * mouseDx + mouseDy * mouseDy) / dt;

                // Direction from the mouse toward the (new) target position.
                double towardX = jump.targetX() - a.mouseX();
                double towardY = jump.targetY() - a.mouseY();
                double dot = mouseDx * towardX + mouseDy * towardY;

                if (mouseSpeed > MOVE_ONSET_PX_PER_SEC && dot > 0) {
                    double latencyMs = (b.timeSeconds() - jump.timeSeconds()) * 1000.0;
                    latencies.add(latencyMs);
                    break;
                }

                // Stop searching if the next jump arrives before any reaction.
                double nextTargetMove = distance(
                        a.targetX(), a.targetY(), b.targetX(), b.targetY());
                if (nextTargetMove >= JUMP_DETECT_PX) {
                    break;
                }
            }
        }
        return latencies;
    }

    private double average(List<Double> values) {
        if (values.isEmpty()) {
            return Double.NaN;
        }
        double sum = 0;
        for (double v : values) {
            sum += v;
        }
        return sum / values.size();
    }

    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
