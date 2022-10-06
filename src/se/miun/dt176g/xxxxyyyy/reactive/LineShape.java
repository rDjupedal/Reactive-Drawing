package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;

public class LineShape extends AbstractShape{

    public LineShape(int x, int y, Color color, int thickness) {
        setColor(color);
        setThickness(thickness);

        setStartX(x);
        setStartY(y);
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(getColor());
        g2.setStroke(new BasicStroke(getThickness()));
        g2.drawLine(getStartX(), getStartY(), getDrawToX(), getDrawToY());

    }

}
