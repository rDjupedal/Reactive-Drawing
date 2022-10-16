package se.miun.dt176g.xxxxyyyy.reactive;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

public class DrawView extends JPanel {
    private final int maxThickness = 25;
    private final MainFrame frame;
    private ArrayList<JButton> shapeButtons = new ArrayList<>();
    private JSpinner thicknessSpinner;
    private JButton colorBtn;
    private JPanel buttonPanel;
    private JPanel drawPanel;
    private JPanel connectionPanel;

    public DrawView(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout(10,5));

        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.GRAY);
        setupControls(buttonPanel);
        add(buttonPanel, BorderLayout.NORTH);

        drawPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                ArrayList<AbstractShape> shapes = frame.getController().getShapes();
                shapes.forEach(shape -> shape.paint(g));
            }
        };

        add(drawPanel, BorderLayout.CENTER);

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
        thicknessSpinner = new JSpinner(new SpinnerNumberModel(3, 1, maxThickness, 1));
        panel.add(thicknessSpinner);
    }

    protected ArrayList<JButton> getShapeBtns() { return this.shapeButtons; }

    protected JSpinner getThickSpinner() { return thicknessSpinner; }

    protected JButton getColorBtn() { return colorBtn; }

    protected JPanel getDrawPanel() { return drawPanel; }

    protected JPanel getConnectionPanel() { return connectionPanel; }

}
