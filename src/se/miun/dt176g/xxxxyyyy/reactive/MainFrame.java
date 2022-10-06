package se.miun.dt176g.xxxxyyyy.reactive;

import javax.swing.*;

public class MainFrame extends JFrame {
    DrawModel dModel;
    DrawView dView = new DrawView(this);
    DrawController dController;

    public MainFrame() {
        this.setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("MS Paint sucks!");

        dModel = new DrawModel();
        dController = new DrawController(dView, dModel);

        add(dView);
    }

    public DrawController getController() { return dController; }
}
