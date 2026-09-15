package particlesim.ui;

import particlesim.analysis.MetricsEngine;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Shown after a test completes. Redesigned to lead with a single headline stat
 * (average reaction latency) plus two compact secondary lines, and to tuck the
 * full per-jump table behind a "Show jump-by-jump detail" toggle instead of
 * always showing it.
 *
 * This panel only *renders* results it is handed; it performs no analysis
 * itself, keeping the metric math in the analysis package.
 */
public class ResultsPanel extends JPanel {

    private final JLabel titleLabel = new JLabel();

    // The scrollable per-jump table, revealed/hidden by the toggle button.
    private final JPanel detailContainer = new JPanel(new BorderLayout());
    private final JButton detailToggle = new JButton("Show jump-by-jump detail");
    private boolean detailVisible = false;

    // The stacked content area that gets rebuilt on each showResults() call.
    private final JPanel content = new JPanel();

    public ResultsPanel(Runnable onTryAgain) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.add(titleLabel, BorderLayout.NORTH);
        top.add(content, BorderLayout.CENTER);

        // The detail toggle just flips the visibility of the table container.
        detailContainer.setVisible(false);
        detailContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailToggle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailToggle.addActionListener(e -> toggleDetail());

        // --- Bottom: disclaimer above the Try Again button -----------------
        JLabel disclaimer = new JLabel(
                "This is a simplified model for a course project, "
                        + "not a clinical or diagnostic tool.");
        disclaimer.setFont(new Font("SansSerif", Font.ITALIC, 11));
        disclaimer.setForeground(Color.GRAY);

        JButton tryAgain = new JButton("Try Again");
        tryAgain.addActionListener(e -> onTryAgain.run());

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        JPanel disclaimerRow = new JPanel(new BorderLayout());
        disclaimerRow.add(disclaimer, BorderLayout.WEST);
        JPanel buttonRow = new JPanel();
        buttonRow.add(tryAgain);
        bottom.add(disclaimerRow, BorderLayout.NORTH);
        bottom.add(buttonRow, BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Populate the panel from a freshly computed result. */
    public void showResults(MetricsEngine.Result result) {
        titleLabel.setText(result.saccadic
                ? "Saccadic Test Results"
                : "Smooth Pursuit Test Results");

        content.removeAll();

        boolean hasLatencies = result.saccadic
                && !result.reactionLatenciesMs.isEmpty();

        if (hasLatencies) {
            // 1. Headline: average reaction latency.
            addHeadline(String.format("Average reaction latency: %.0f ms",
                    result.averageReactionLatencyMs));
            addSubtitle(String.format("(based on %d jumps)",
                    result.reactionLatenciesMs.size()));

            content.add(Box.createVerticalStrut(18));

            // 2. Two compact secondary lines.
            addSecondary(String.format(
                    "Tracking accuracy: %.0f px average deviation",
                    result.averageDeviationPx));
            addSecondary(String.format(
                    "Fastest reaction: %.0f ms   |   Slowest: %.0f ms",
                    result.getMinLatencyMs(), result.getMaxLatencyMs()));

            content.add(Box.createVerticalStrut(18));

            // 4. Collapsed per-jump detail behind a toggle.
            rebuildDetailTable(result);
            content.add(detailToggle);
            content.add(detailContainer);
        } else {
            // No jumps to report (e.g. smooth-pursuit run): lead with accuracy.
            addHeadline(String.format(
                    "Tracking accuracy: %.0f px average deviation",
                    result.averageDeviationPx));
            addSubtitle(result.saccadic
                    ? "(no jumps were measured)"
                    : "(smooth pursuit - continuous tracking)");
            // Nothing to expand, so keep the detail toggle hidden.
            detailContainer.setVisible(false);
        }

        revalidate();
        repaint();
    }

    // ------------------------------------------------------------- helpers

    private void addHeadline(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 26));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(label);
    }

    private void addSubtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(label);
    }

    private void addSecondary(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(label);
        content.add(Box.createVerticalStrut(4));
    }

    private void toggleDetail() {
        detailVisible = !detailVisible;
        detailContainer.setVisible(detailVisible);
        detailToggle.setText(detailVisible
                ? "Hide jump-by-jump detail"
                : "Show jump-by-jump detail");
        revalidate();
        repaint();
    }

    /** Rebuild the (initially hidden) per-jump reaction-latency table. */
    private void rebuildDetailTable(MetricsEngine.Result result) {
        detailContainer.removeAll();
        detailContainer.add(Box.createVerticalStrut(8), BorderLayout.NORTH);

        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < result.reactionLatenciesMs.size(); i++) {
            rows.add(new String[]{
                    "Jump " + (i + 1),
                    String.format("%.0f ms", result.reactionLatenciesMs.get(i))});
        }
        detailContainer.add(
                makeTable(new String[]{"Jump #", "Reaction latency"}, rows),
                BorderLayout.CENTER);

        // Start collapsed each time a new result is shown.
        detailVisible = false;
        detailContainer.setVisible(false);
        detailToggle.setText("Show jump-by-jump detail");
    }

    /** Build a read-only two-column table sized to fit its rows. */
    private JScrollPane makeTable(String[] columns, List<String[]> rows) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // results are display-only
            }
        };
        for (String[] row : rows) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setEnabled(false);

        JScrollPane scroll = new JScrollPane(table);
        int height = table.getRowHeight() * rows.size()
                + table.getTableHeader().getPreferredSize().height;
        int width = 320;
        scroll.setPreferredSize(new Dimension(width, height + 2));
        scroll.setMaximumSize(new Dimension(width, height + 2));
        return scroll;
    }
}
