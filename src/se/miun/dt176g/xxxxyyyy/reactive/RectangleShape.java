package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;

public class RectangleShape extends AbstractShape{

    public RectangleShape(int x, int y, Color color, int thickness) {

        setColor(color);
        setThickness(thickness);

        setStartX(x);
        setStartY(y);
    }

    @Override
    public void paint(Graphics g) {
        int x1 = getStartX();
        int y1 = getStartY();
        int x2 = getDrawToX();
        int y2 = getDrawToY();

        int width = Math.abs(x1 - x2);
        int height = Math.abs(y1 - y2);

        if (x1 > x2) x1 = x2;
        if (y1 > y2) y1 = y2;

        /*
        g.setColor(getColor());
        g.drawRect(x1, y1, width, height);
         */

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getColor());
        g2.setStroke(new BasicStroke(getThickness()));
        g2.drawRect(x1, y1, width, height);

    }

}
