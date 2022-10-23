package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import static java.lang.System.out;

/**
 * Wrapper to keep track of which socket incoming shapes originate from
 *
 * @author Rasmus Djupedal
 */
class ShapeSocketPair {
    private AbstractShape shape;
    private Socket socket;

    protected ShapeSocketPair(AbstractShape shape, Socket socket) {
        this.shape = shape;
        this.socket = socket;
    }
    protected Socket getSocket() {return this.socket; }
    protected AbstractShape getShape() {return this.shape; }
}

/**
 * Server for retrieving and sending shapes
 *
 * @author Rasmus Djupedal
 */
public class Server {

    private static final int SERVER_PORT = 5000;
    private ReplaySubject<ShapeSocketPair> shapeStream = ReplaySubject.create();
    private HashMap<Integer, Disposable> disposableHashMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("ShapeServer started a port " + SERVER_PORT);

        shapeStream
                .doOnNext(s -> out.println("Received shape " + s.getShape().getClass().getName() + " on socket " + s.getSocket().toString() + ". Thread: " + Thread.currentThread().getName()))
                .subscribe();

        while (true) {
            Socket socket = serverSocket.accept();  // A new Socket is created for the incoming connection
            Observable.<Socket>create(emitter -> emitter.onNext(socket))
                    .observeOn(Schedulers.io())
                    .subscribe(this::connect);
        }

        //not reachable

    }

    private void connect(Socket socket) {
        out.println("Connection accepted on thread: " + Thread.currentThread().getName());

        // Subscribe the socket to the shapeStream (send all incoming shapes to the connected client)
        try {
            ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());
            shapeStream
                    .subscribeOn(Schedulers.io())
                    // Put the observable into container
                    .doOnSubscribe(observable -> disposableHashMap.put(socket.hashCode(), observable))
                    // Only send shapes that do not originate from the same socket
                    .filter(shapeSocketPair -> shapeSocketPair.getSocket() != socket)
                    .doOnNext((s) -> System.out.println("Sending shape"))
                    .map(ShapeSocketPair::getShape)
                    .subscribe(
                            objOutStream::writeObject
                            , err -> System.out.println("Error sending shape to client: " +err));

        } catch (Exception e) { e.printStackTrace(); }


        // Subscribe to receive shapes
        Observable.<ShapeSocketPair>create(emitter -> {
            Observable.just(socket)
                    .map(Socket::getInputStream)
                    .map(ObjectInputStream::new)
                    .subscribe(inStream -> {
                        while(!emitter.isDisposed()) {
                            Object shape = inStream.readObject();
                            if (shape == null || socket.isClosed()) {
                                emitter.onError(new Throwable("Connection error"));
                            }
                            else {
                                emitter.onNext((new ShapeSocketPair((AbstractShape) shape, socket)));
                            }
                        }
                    }
                    , err -> {
                        if (socket.isConnected()) socket.close();
                        emitter.onError(new Throwable("Socket disconnected"));
                    });
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(shapeSocket -> System.out.println("received " + shapeSocket.getShape().getClass().getName()))
                .subscribe(
                        shapeStream::onNext
                        , err -> {
                            System.out.println(err.getMessage());
                            // Dispose the Observable using a Throwable to carry the hash code
                            // of the socket out of this lambda expression.
                            disposeClient(new Throwable(Integer.toString(socket.hashCode())));
                        });
    }

    private void disposeClient(Throwable throwable) {
        int socketHash = Integer.parseInt(throwable.getMessage());
        System.out.println("Disposing client socket " + socketHash);
        disposableHashMap.get(socketHash).dispose();
        disposableHashMap.remove(socketHash);
    }


}
