package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import java.util.ArrayList;

/**
 * Tool to draw free hand shapes.
 *
 * @author Rasmus Djupedal
 */
public class FreeHandShape extends AbstractShape {
    private ArrayList<Point> points = new ArrayList<>();

    public FreeHandShape(Point startPoint, Color color, int thickness) {
        setColor(color);
        setThickness(thickness);
        points.add(startPoint);
    }

    @Override
    public void dragTo(Point point) {
        points.add(point);
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getColor());
        g2.setStroke(new BasicStroke(getThickness()));

        for (int i = 1; i < points.size(); i++) {
            g2.drawLine(points.get(i - 1).getX(), points.get(i - 1).getY(), points.get(i).getX(), points.get(i).getY());
        }
    }


}
