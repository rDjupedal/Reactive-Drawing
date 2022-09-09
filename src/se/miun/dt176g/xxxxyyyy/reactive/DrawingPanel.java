package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import javax.swing.*;

/**
 * <h1>DrawingPanel</h1> Creates a Canvas-object for displaying all graphics
 * already drawn.
 *
 * @author 	Rasmus Djupedal
 * @version 1.0
 * @since 	2022-09-08
 */

@SuppressWarnings("serial")
public class DrawingPanel extends JPanel {

	private Drawing drawing;
	

	public DrawingPanel() {
		drawing = new Drawing();
	}
	
	public void redraw() {
		repaint();
	}

	public void setDrawing(Drawing d) {
		drawing = d;
		repaint();
	}

	public Drawing getDrawing() {
		return drawing;
	}

	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);
		drawing.draw(g);
	}

}
