package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import java.io.Serializable;

/**
 * Abstract class from which all concrete shapes (including FreeHandShape) inherit
 * Implements Serializable in order to be able to send it over a Socket
 *
 * @author Rasmus Djupedal
 */
public abstract class AbstractShape implements Serializable {
    private Point startPoint, endPoint;
    private int thickness = 2;
    private Color color;

    /**
     * Each concrete shape overrides this method with their own implementation on how to draw the specific shape
     * @param g The Graphics adapter
     */
    public void paint(Graphics g) {
        System.out.println("paint method for this shape not yet implemented..");
    }

    /**
     * Shape is being drawn
     * @param point The new position of the mouse pointer
     */
    public void dragTo(Point point){
        this.endPoint = point;
    }

    /**
     * When starting to draw a new shape set both the start and the end points to the start position
     * @param sPoint The start position of the new shape
     */
    public void setStartPoint(Point sPoint) {
        this.startPoint = new Point(sPoint.getX(), sPoint.getY());
        this.endPoint = new Point(sPoint.getX(), sPoint.getY());
    }

    public int getStartX() { return startPoint.getX(); }
    public int getStartY() { return startPoint.getY(); }
    public int getDrawToX() { return endPoint.getX(); }
    public int getDrawToY() { return endPoint.getY(); }
    public Color getColor() { return color; }
    public int getThickness() { return thickness; }
    public void setColor(Color color) { this.color = color; }
    public void setThickness(int thickness) { this.thickness = thickness; }
}
