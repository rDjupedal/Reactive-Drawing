package se.miun.dt176g.xxxxyyyy.reactive;

import javax.swing.*;

/**
 * The frame containing the main GUI..
 *
 * @author Rasmus Djupedal
 */
public class MainFrame extends JFrame {
    DrawModel dModel;
    DrawView dView = new DrawView(this);
    DrawController dController;

    public MainFrame() {
        this.setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ShapeDrawer!");

        dModel = new DrawModel();
        dController = new DrawController(dView, dModel);

        add(dView);
    }

    public DrawController getController() { return dController; }
}
