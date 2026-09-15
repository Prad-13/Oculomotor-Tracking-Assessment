package particlesim;

import particlesim.analysis.MetricsEngine;
import particlesim.analysis.Tracker;
import particlesim.model.Target;
import particlesim.model.World;
import particlesim.ui.ControlPanel;
import particlesim.ui.ResultsPanel;
import particlesim.ui.TestPanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

/**
 * Application entry point and composition root.
 *
 * Wires together the model, the analysis pieces, and the three UI views, and
 * uses a CardLayout to switch between the test view and the results view. This
 * is the only class that knows about every package at once.
 *
 * Repurposed from the original particle-simulator Main: it no longer builds a
 * PhysicsEngine/CollisionHandler (those classes were removed); instead it builds
 * the Tracker + MetricsEngine and connects the test run to the results screen.
 */
public class Main {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double TARGET_RADIUS = 14;

    // Card names for the CardLayout.
    private static final String TEST_CARD = "test";
    private static final String RESULTS_CARD = "results";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::createAndShow);
    }

    private static void createAndShow() {
        // --- Model + analysis ----------------------------------------------
        Target target = new Target(TARGET_RADIUS);
        World world = new World(WIDTH, HEIGHT, target);
        Tracker tracker = new Tracker();
        MetricsEngine metricsEngine = new MetricsEngine();

        // --- Card container -------------------------------------------------
        CardLayout cards = new CardLayout();
        JPanel cardContainer = new JPanel(cards);

        // --- Test view (target panel + controls) ---------------------------
        TestPanel testPanel = new TestPanel(world, tracker);
        testPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        ControlPanel controlPanel = new ControlPanel(testPanel);

        JPanel testView = new JPanel(new BorderLayout());
        testView.add(testPanel, BorderLayout.CENTER);
        testView.add(controlPanel, BorderLayout.SOUTH);

        // --- Results view ---------------------------------------------------
        // "Try Again" flips back to the test view and resets it.
        ResultsPanel resultsPanel = new ResultsPanel(() -> {
            testPanel.reset();
            cards.show(cardContainer, TEST_CARD);
        });

        cardContainer.add(testView, TEST_CARD);
        cardContainer.add(resultsPanel, RESULTS_CARD);

        // When a run finishes: compute metrics, show them, switch to results.
        testPanel.setCompletionListener((data, saccadic) -> {
            MetricsEngine.Result result = metricsEngine.analyze(data, saccadic);
            resultsPanel.showResults(result);
            cards.show(cardContainer, RESULTS_CARD);
        });

        // --- Frame ----------------------------------------------------------
        JFrame frame = new JFrame("Oculomotor Tracking Test (educational demo)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(cardContainer);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
