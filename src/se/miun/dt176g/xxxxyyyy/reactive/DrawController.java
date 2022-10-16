package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class DrawController {
    private final DrawView dView;
    private final DrawModel dModel;
    private Observable<Socket> socketObservable;

    public DrawController(DrawView dView, DrawModel dModel) {
        this.dView = dView;
        this.dModel = dModel;

        //todo: make this an observable too.. or is that too much?
        dView.getDrawPanel().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dModel.createShape(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // if a new shape has been drawn now is probably the time to send it to the server
                AbstractShape curShape = dModel.getCurrentShape();
                System.out.println("finished drawing shape: " + curShape.getClass().getName());

                socketObservable
                        .subscribeOn(Schedulers.io())
                        .map(socket -> socket.getOutputStream())
                        .map(ObjectOutputStream::new)
                        .subscribe(oStream -> oStream.writeObject(curShape));

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


        //Observable<Socket> socketObservable = Observable.create(emitter -> {
        socketObservable = Observable.create(emitter -> {

            // Get the controls from connectionPanel
            JButton connBtn = null;
            JTextField addressTextField = null;
            Component[] components = dView.getConnectionPanel().getComponents();
            for (Component c : components) {
                if (c.getName().equals("hostText")) addressTextField = ((JTextField) c);
                if (c.getName().equals("connBtn")) connBtn = (JButton) c;
            }

            final JTextField finalAddressTextField = addressTextField;
                connBtn.addActionListener(l -> {
                    String host = finalAddressTextField.getText().split("/")[0];
                    int port = Integer.parseInt(finalAddressTextField.getText().split("/")[1]);
                    System.out.println("Connecting to " + host + " on port " + port + "...");
                    //todo Check legal address
                    try {
                        emitter.onNext(new Socket(host, port));
                    } catch (Exception e) { e.printStackTrace(); }

                });

        });


        toolObservable.subscribe(tool -> dModel.setTool(tool));
        thicknessObservable.subscribe(thickValue -> dModel.setThickness(thickValue));
        colorObservable.subscribe(color -> dModel.setColor(color));

        socketObservable
                .subscribeOn(Schedulers.io())
                .map(ShapeReceiver::new)
                .subscribe(obsShape -> {
                    obsShape.getObserver()
                            .subscribeOn(Schedulers.io())    // <--- Important! To not wait for shapes on main thread!
                            .doOnNext(s -> System.out.println("got shape: " + s.getClass().getName()))
                            .subscribe(shape -> { dModel.addShape(shape);

                            });
                });

    }


    protected ArrayList<AbstractShape> getShapes() {
        return dModel.getShapes();
    }

    private Color getComplementaryColor(Color color) {
        return new Color(-1 - color.getRGB());
    }
}
