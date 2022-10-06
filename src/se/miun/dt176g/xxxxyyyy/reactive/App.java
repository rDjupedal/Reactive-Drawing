package se.miun.dt176g.xxxxyyyy.reactive;


import javax.swing.*;

/**
 * Hello world!
 *
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
