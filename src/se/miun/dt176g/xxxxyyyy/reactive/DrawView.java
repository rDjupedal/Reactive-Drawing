package se.miun.dt176g.xxxxyyyy.reactive;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * This is the View part of the MVC pattern
 * and acts as the container for the canvas and the buttons
 *
 * @author Rasmus Djupedal
 */
public class DrawView extends JPanel {
    private final static int MAX_THICKNESS = 25;
    private final MainFrame frame;
    private final ArrayList<JButton> shapeButtons = new ArrayList<>();
    private JSpinner thicknessSpinner;
    private JButton colorBtn;
    private JButton clearBtn;
    private JPanel buttonPanel;
    private JPanel drawPanel;
    private JPanel connectionPanel;

    public DrawView(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout(10,5));

        // Button panel
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.GRAY);
        setupControls(buttonPanel);
        add(buttonPanel, BorderLayout.NORTH);

        drawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                ArrayList<AbstractShape> shapes = frame.getController().getShapes();

                // Clone the ArrayList to avoid concurrency-errors
                ArrayList<AbstractShape> cl = (ArrayList) shapes.clone();
                //shapes.forEach(shape -> shape.paint(g));
                cl.forEach(shape -> shape.paint(g));

            }
        };

        add(drawPanel, BorderLayout.CENTER);

        // Connection panel
        connectionPanel = new JPanel();
        connectionPanel.setBackground(Color.GRAY);
        JTextField serverTextfield = new JTextField("127.0.0.1/5000");
        serverTextfield.setName("hostText");
        JButton connectBtn = new JButton("Connect to server");
        connectBtn.setName("connBtn");
        connectionPanel.add(serverTextfield);
        connectionPanel.add(connectBtn);
        add(connectionPanel, BorderLayout.SOUTH);

    }

    /**
     * Setup controls
     * @param panel panel to host the controls
     */
    private void setupControls(JPanel panel) {

        for (ToolsEnum tool : ToolsEnum.values()) {
            JButton button = new JButton(String.valueOf(tool));
            panel.add(button);
            shapeButtons.add(button);
        }

        colorBtn = new JButton("Color");
        colorBtn.setBackground(Color.BLACK);
        colorBtn.setForeground(Color.WHITE);

        panel.add(colorBtn);

        panel.add(new JLabel("Thickness "));
        thicknessSpinner = new JSpinner(new SpinnerNumberModel(3, 1, MAX_THICKNESS, 1));
        panel.add(thicknessSpinner);

        clearBtn = new JButton("Clear");
        panel.add(clearBtn);
    }

    protected ArrayList<JButton> getShapeBtns() { return this.shapeButtons; }

    protected JSpinner getThickSpinner() { return thicknessSpinner; }

    protected JButton getColorBtn() { return colorBtn; }

    protected JButton getClearBtn() { return clearBtn; }

    protected JPanel getDrawPanel() { return drawPanel; }

    protected JPanel getConnectionPanel() { return connectionPanel; }

}
