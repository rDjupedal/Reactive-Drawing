package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import java.util.ArrayList;

/**
 * This class forms the Model of the MVC pattern
 * It stores all the shapes drawn by client and incoming ones from the server
 * as well as currently selected tool, color and thickness.
 *
 * @author Rasmus Djupedal
 */

public class DrawModel {

    private ToolsEnum selectedTool;
    private Color color = Color.BLACK;
    private int thickness = 3;
    private final ArrayList<AbstractShape> shapes = new ArrayList<>();
    private int currentShapeIndex;

    public DrawModel() {
        this.selectedTool = ToolsEnum.Freehand;
    }

    public void setTool(ToolsEnum tool) {
        this.selectedTool = tool;
    }

    public void createShape(Point point) {
        AbstractShape currentShape;

        switch (selectedTool) {
            case Freehand -> currentShape = new FreeHandShape(point, this.color, this.thickness);
            case Rectangle -> currentShape = new RectangleShape(point, this.color, this.thickness);
            case Line -> currentShape = new LineShape(point, this.color, this.thickness);
            case Oval -> currentShape = new OvalShape(point, this.color, this.thickness);
            default -> {
                System.out.println("Shape not implemented, defaulting to freehand..");
                currentShape = new FreeHandShape(point, this.color, this.thickness);
            }
        }

        shapes.add(currentShape);
        currentShapeIndex = shapes.indexOf(currentShape);
    }

    /**
     * Is called while a shape is still being drawn (on mouse move).
     * @param point The point to which the mouse pointer was moved
     */
    public void dragTo(Point point) {
        shapes.get(currentShapeIndex).dragTo(point);
    }

    protected Color getColor() { return color ;}
    protected void setColor(Color color) { this.color = color; }
    protected void setThickness(int thickness) { this.thickness = thickness; }

    public ArrayList<AbstractShape> getShapes() { return shapes; }

    /**
     * Used for adding incoming shape from server
     * @param shape the shape
     */
    protected void addShape(AbstractShape shape) {
        shapes.add(shape);
    }

    protected void clear() {
        shapes.clear();
    }

    protected AbstractShape getCurrentShape() {
        return shapes.get(currentShapeIndex);
    }

}
