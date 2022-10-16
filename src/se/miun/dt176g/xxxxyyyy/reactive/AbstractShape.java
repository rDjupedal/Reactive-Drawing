package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import java.io.Serializable;

public abstract class AbstractShape implements Serializable {
    private int x1, y1, x2, y2;
    private int thickness = 2;
    private Color color;

    public void paint(Graphics g) {
        System.out.println("paint method for this shape not yet implemented..");
    }

    public void dragTo(Point point){
        x2 = point.getX();
        y2 = point.getY();
    }

    public void setStartX(int startX) {
        this.x1 = startX;
        this.x2 = startX;
    }
    public void setStartY(int startY) {
        this.y1 = startY;
        this.y2 = startY;
    }

    public int getStartX() { return x1; }
    public int getStartY() { return y1; }
    public int getDrawToX() { return x2; }
    public int getDrawToY() { return y2; }
    public Color getColor() { return color; }
    public int getThickness() { return thickness; }
    public void setColor(Color color) { this.color = color; }
    public void setThickness(int thickness) { this.thickness = thickness; }

}
