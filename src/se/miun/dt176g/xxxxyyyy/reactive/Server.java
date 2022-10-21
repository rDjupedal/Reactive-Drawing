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


public class Server {

    private ReplaySubject<AbstractShape> shapeStream = ReplaySubject.create();
    private HashMap<Integer, Disposable> disposableHashMap = new HashMap<>();


    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.startServer();
    }

    public void startServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(5000);

        shapeStream
                .doOnNext(s -> out.println("shapeStream ReplaySubject received emission. Thread: " + Thread.currentThread().getName()))
                .subscribe();

        while (true) {
            Socket socket = serverSocket.accept();  // A new Socket is created for the incoming connection
            Observable.<Socket>create(emitter -> emitter.onNext(socket))
                    .observeOn(Schedulers.io())
                    .subscribe(this::connect
                    , err -> System.out.println("debug error here"));
        }

        //not reachable

    }

    private void connect(Socket socket) {
        out.println("Connection accepted on thread: " + Thread.currentThread().getName());


        // Subscribe the socket to the shapeStream (sending shapes)
        try {
            ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());
            shapeStream
                .subscribeOn(Schedulers.io())
                    .doOnSubscribe(observable -> {
                        System.out.println("putting observable into container");
                        disposableHashMap.put(socket.hashCode(), observable);
                    })
                    .doOnNext((s) -> System.out.println("sending object"))
                .subscribe(
                        objOutStream::writeObject
                        , err -> System.out.println("error sending shape to client: " +err));


        } catch (Exception e) { e.printStackTrace(); }


        // Subscribe to receive shapes
        Observable.<AbstractShape>create(emitter -> {
            Observable.just(socket)
                    .map(Socket::getInputStream)
                    .map(ObjectInputStream::new)
                    .subscribe(inStream -> {
                        while(!emitter.isDisposed()) {
                            Object shape = inStream.readObject();
                            if (shape == null || socket.isClosed()) {
                                System.out.println("Shape not received");
                                emitter.onError(new Throwable("Connection error"));
                            }
                            else {
                                emitter.onNext((AbstractShape) shape);
                            }
                        }
                    }
                    , err -> {
                        if (socket.isConnected()) socket.close();
                        emitter.onError(new Throwable("Socket disconnected"));
                    });
        })
                .subscribeOn(Schedulers.io())
                //.doOnError(this::disposeClient)
                .doOnNext(shape -> System.out.println("received " + shape.getClass().getName()))
                .subscribe(
                        shapeStream::onNext
                        , err -> {
                            System.out.println(err.getMessage());
                            // Stop this socket from subscribing to new shapes
                            // Using a Throwable to carry the hash code of the socket..
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
