package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class DrawController {
    private final DrawView dView;
    private final DrawModel dModel;

    private Socket socket = null;
    private ObjectOutputStream outputStream = null;
    private Disposable inShapes;
    private Boolean isConnected = false;


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

                if (isConnected) {

                    // A shape has been finished drawing, set it to the server
                    AbstractShape curShape = dModel.getCurrentShape();
                    Observable.just(outputStream)
                            .subscribeOn(Schedulers.io())
                            .subscribe(stream -> stream.writeObject(curShape),
                                    err -> System.out.println("error: " + err));
                }

                /*
                if (socket != null) {
                    Observable.just(socket)
                            .filter(socket -> socket.isConnected())
                            .map(Socket::getOutputStream)
                            .map(ObjectOutputStream::new)
                            .subscribe(outStream -> outStream.writeObject(curShape), err -> System.err.println(err));
                }

                 */

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


            dView.getClearBtn().addActionListener(listener -> {
                dModel.clear();
                dView.repaint();
            });



        /*
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
                        //emitter.onNext(new Socket(host, port));
                        connectToServer(host, port);
                    } catch (Exception e) { e.printStackTrace(); }

                });

        });

         */


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

            try {
                String host = finalAddressTextField.getText().split("/")[0];
                int port = Integer.parseInt(finalAddressTextField.getText().split("/")[1]);
                connectToServer(host, port);
            } catch (Exception e) {
                System.out.println("Malformed server/port input");
                e.printStackTrace();
            }
        });


        toolObservable.subscribe(tool -> dModel.setTool(tool));
        thicknessObservable.subscribe(thickValue -> dModel.setThickness(thickValue));
        colorObservable.subscribe(color -> dModel.setColor(color));

    }

    private void connectToServer(String host, int port) throws IOException {
        System.out.println("Connecting to " + host + " on port " + port + "...");

        // The Socket and the ObjectOutStream can't be inside the Observable as these same instances are needed for sending also
        socket = new Socket(host, port);

        outputStream = new ObjectOutputStream(socket.getOutputStream());
        isConnected = true;

        // Subscribe to incoming shapes


        ShapeReceiver sReceiver = new ShapeReceiver(socket);
        inShapes = sReceiver.getObserver()
                .subscribeOn(Schedulers.io())
                .doOnNext(s -> System.out.println("got shape: " + s.getClass().getName() + " on thread " + Thread.currentThread().getName()))
                .subscribe((shape) -> {
                    dModel.addShape(shape);
                    dView.repaint();
                    }
                , err -> System.out.println("Error receiving shape: " + err)
                , () -> System.out.println("Observable finished"));




        /*
        // Subscribe to incoming shapes
        Observable.just(socket)
                .subscribeOn(Schedulers.io())
                .map(ShapeReceiver::new)
                .subscribe(obsShape -> {
                    obsShape.getObserver()
                            .subscribeOn(Schedulers.io())    // <--- Important! To not wait for shapes on main thread!
                            .doOnNext(s -> System.out.println("got shape: " + s.getClass().getName() + " on thread " + Thread.currentThread().getName()))

                            .subscribe((shape) -> {
                                    dModel.addShape(shape);
                                    dView.repaint();
                            }, err -> System.out.println("Error retrieving socket: " + err));
                            //.subscribe(dModel::addShape, err -> System.out.println("Error retrieving socket: " + err));

                });

         */

    }

    private void disconnect() {
        isConnected = false;
        inShapes.dispose();
    }


    protected ArrayList<AbstractShape> getShapes() {
        return dModel.getShapes();
    }

    private Color getComplementaryColor(Color color) {
        return new Color(-1 - color.getRGB());
    }
}
