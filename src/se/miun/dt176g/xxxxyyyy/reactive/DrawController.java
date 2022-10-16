package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DrawController {
    private final DrawView dView;
    private final DrawModel dModel;

    public DrawController(DrawView dView, DrawModel dModel) {
        this.dView = dView;
        this.dModel = dModel;

        //todo: make this an observable too.. or is that too much?
        dView.getDrawPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dModel.createShape(e.getX(), e.getY());
            }

            //todo: Remove if not used in future
            @Override
            public void mouseReleased(MouseEvent e) {
                // if a new shape has been drawn now is probably the time to send it to the server
            }
        });

        // Observable MouseDrags
        Observable<Point> mouseObs = Observable.create(emitter -> {

            dView.getDrawPanel().addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    //super.mouseDragged(e);
                    emitter.onNext(new Point(e.getX(), e.getY()));
                }
            });

        });

        mouseObs.subscribe(point -> {
            dModel.dragTo(point);
            dView.repaint();
        });

        setupBtnObservers();
    }


    /**
     * Fetch the buttons from the view and make them Observable, and subscribe to them
     */
    protected void setupBtnObservers() {

        Observable<ToolsEnum> toolObservable = Observable.create(emitter -> {
            ArrayList<JButton> buttons = dView.getShapeBtns();
            for (JButton btn : buttons) {
                btn.addActionListener(listener -> {
                    emitter.onNext(ToolsEnum.valueOf(btn.getText()));
                });
            }
        });

        JSpinner tSpinner = dView.getThickSpinner();
        Observable<Integer> thicknessObservable = Observable.create(emitter -> {

            tSpinner.addChangeListener(listener -> {
                emitter.onNext((Integer) tSpinner.getValue());
            });
        });

        Observable<Color> colorObservable = Observable.create(emitter -> {

            JButton colorBtn = dView.getColorBtn();
            colorBtn.addActionListener(listener -> {
                Color color = JColorChooser.showDialog(null, "Välj färg", dModel.getColor());
                if (color == null) return;      // If no color was chosen don't overwrite with null..
                colorBtn.setBackground(color);
                colorBtn.setForeground(getComplementaryColor(color));
                emitter.onNext(color);
            });
        });

        toolObservable.subscribe(tool -> dModel.setTool(tool));
        thicknessObservable.subscribe(thickValue -> dModel.setThickness(thickValue));
        colorObservable.subscribe(color -> dModel.setColor(color));
    }

    protected ArrayList<AbstractShape> getShapes() {
        return dModel.getShapes();
    }

    private Color getComplementaryColor(Color color) {
        return new Color(-1 - color.getRGB());
    }
}
