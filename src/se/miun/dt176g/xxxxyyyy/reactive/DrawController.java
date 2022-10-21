package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DrawController {
    private final int TIMEOUT = 10000;  // Timeout for outgoing connections
    private final DrawView dView;
    private final DrawModel dModel;

    private Socket socket = null;
    private ObjectOutputStream outputStream = null;
    private Disposable inShapes;
    private Boolean isConnected = false;
    private PublishSubject<Integer> connectionState = PublishSubject.create();  // 0 disconnected, 1 connecting, 2 connected
    private JButton connBtn = null;
    JTextField addressTextField = null;


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

                // A shape has been finished drawing, send it to the server
                if (isConnected) {
                    Observable.just(outputStream)
                            .observeOn(Schedulers.io())
                            .subscribe(stream -> stream.writeObject(dModel.getCurrentShape()),
                                    err -> {
                                System.out.println("error: " + err.getMessage());
                                connectionState.onNext(0);
                                    });
                }
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

        //Clear the canvas
        dView.getClearBtn().addActionListener(listener -> {
            dModel.clear();
            dView.repaint();
        });

        // Get the controls from connectionPanel
        Component[] components = dView.getConnectionPanel().getComponents();
        for (Component c : components) {
            if (c.getName().equals("hostText")) addressTextField = ((JTextField) c);
            if (c.getName().equals("connBtn")) connBtn = (JButton) c;
        }

        //Connect button
         connBtn.addActionListener(l -> {
            if (isConnected) connectionState.onNext(0);   // Disconnect
            else {
                Observable.just(addressTextField)
                        .map(JTextField::getText)
                        .doOnNext((s) -> connectionState.onNext(1)) // Set connectionState to "connecting"
                        .map(arg -> new Socket(arg.split("/|:")[0], Integer.parseInt(arg.split("/|:")[1])))
                        .subscribeOn(Schedulers.io())       // Use a IO thread for the whole Observable
                                                            // to prevent stuff in connectionState to
                                                            // freeze the GUI if something socket-related goes wrong..
                        .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                        .subscribe(
                                this::connectToServer
                                , err -> {
                                    System.err.println(err);
                                    String errMsg = "Unknown error\n" + err.getMessage();

                                    if (err instanceof UnknownHostException ||
                                            err instanceof ArrayIndexOutOfBoundsException) errMsg = "Malformed host!\nPlease type IP/port";
                                    if (err instanceof ConnectException) errMsg = "Host down!\nPlease check IP/port";
                                    if (err instanceof IllegalArgumentException) errMsg = "Port number out of range";
                                    if (err instanceof SecurityException) errMsg = "Not allowed to open connections";
                                    if (err instanceof TimeoutException) errMsg = "Server timeout";

                                    connectionState.onNext(0);  // Set connectionState to disconnected
                                    JOptionPane.showMessageDialog(dView, errMsg);
                                });
            }
        });


        toolObservable.subscribe(tool -> dModel.setTool(tool));
        thicknessObservable.subscribe(thickValue -> dModel.setThickness(thickValue));
        colorObservable.subscribe(color -> dModel.setColor(color));

        // Update the view and dispose Observers depending on current connection status
        connectionState.subscribe(status -> {
            switch (status) {
                case 0 -> { // Disconnected
                    connBtn.setText("Connect");
                    connBtn.setEnabled(true);
                    addressTextField.setEnabled(true);
                    isConnected = false;
                    if (inShapes != null) inShapes.dispose();
                    if (socket != null) socket.close();
                }
                case 1 -> { // Connecting
                    connBtn.setText("Connecting...");
                    connBtn.setEnabled(false);
                    addressTextField.setEnabled(false);
                }
                case 2 -> { // Connected
                    connBtn.setText("Disconnect");
                    connBtn.setEnabled(true);
                    addressTextField.setEnabled(false);
                    isConnected = true;
                }
            }

        });
    }

    private void connectToServer(Socket socket) throws IOException {
        System.out.println("Connecting to " + socket.getInetAddress() + " on port " + socket.getPort() + "...");

        // The Socket and the ObjectOutStream can't be inside the Observable as these same instances are needed for sending also
        this.socket = socket;
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        connectionState.onNext(2);  //set connectionStatus to connected

        // Subscribe to incoming shapes
        ShapeReceiver sReceiver = new ShapeReceiver(socket);
        inShapes = sReceiver.getObserver()
                .subscribeOn(Schedulers.io())
                .doOnNext(s -> System.out.println("got shape: " + s.getClass().getName() + " on thread " + Thread.currentThread().getName()))
                .subscribe((shape) -> {
                    dModel.addShape(shape);
                    dView.repaint();
                    }
                , err -> {
                    System.out.println(err.getMessage());
                    JOptionPane.showMessageDialog(dView, err.getMessage());
                    connectionState.onNext(0);
                    });
    }


    protected ArrayList<AbstractShape> getShapes() {
        return dModel.getShapes();
    }

    private Color getComplementaryColor(Color color) {
        return new Color(-1 - color.getRGB());
    }
}
