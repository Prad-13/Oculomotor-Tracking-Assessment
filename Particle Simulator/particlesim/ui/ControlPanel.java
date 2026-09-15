package particlesim.ui;

import particlesim.movement.TestType;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The controls beneath the test surface. Lets the user pick a test type and
 * start or reset a run.
 *
 * Rewritten from the original particle-sim control panel: the gravity toggle,
 * "Add Particle" button, and speed slider are gone (there is no free-form
 * physics to tune anymore). What remains in spirit is the "Reset" button and the
 * pattern of wiring simple controls to the panel without any logic living here.
 */
public class ControlPanel extends JPanel {

    public ControlPanel(TestPanel testPanel) {

        // --- Test type selection -------------------------------------------
        JLabel typeLabel = new JLabel("Test type:");
        JComboBox<TestType> typeBox = new JComboBox<>(TestType.values());

        // --- Start ----------------------------------------------------------
        JButton startButton = new JButton("Start Test");
        startButton.addActionListener(e -> {
            TestType selected = (TestType) typeBox.getSelectedItem();
            testPanel.startTest(selected);
        });

        // --- Reset ----------------------------------------------------------
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> testPanel.reset());

        add(typeLabel);
        add(typeBox);
        add(startButton);
        add(resetButton);
    }
}
