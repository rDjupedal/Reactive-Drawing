package se.miun.dt176g.xxxxyyyy.reactive;


import javax.swing.*;

/**
 * Entry point for the client part of ShapeDrawer
 *
 * @author Rasmus Djupedal
 */
public class App {
    public static void main( String[] args ) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
