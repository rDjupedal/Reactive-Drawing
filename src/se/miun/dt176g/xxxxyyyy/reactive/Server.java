package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import static java.lang.System.out;


public class Server {

    private ReplaySubject<AbstractShape> shapeStream = ReplaySubject.create();

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
            Socket socket = serverSocket.accept();
            Observable.<Socket>create(emitter -> emitter.onNext(socket))
                    .observeOn(Schedulers.io())
                    .subscribe(this::listenTest);
        }

        //not reachable

    }

    private void listenTest(Socket socket) {
        out.println("Connection accepted on thread: " + Thread.currentThread().getName());

        /*
        Observable.<AbstractShape>create(emitter -> {
            get2(socket)
                    .subscribe(obj -> {
                        while (!emitter.isDisposed()) {
                            AbstractShape shape = (AbstractShape) obj;
                            if (shape == null || socket.isClosed()) {
                                emitter.onError(new ConnectException("socket error"));
                            } else {
                                emitter.onNext(shape);
                            }
                        }
                    });
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(s-> System.out.println("debug"))
                .map(shape -> (AbstractShape) shape)
                .doOnNext(shape -> System.out.println(shape.getClass().getName()));

         */

        Observable.<AbstractShape>create(emitter -> {
            Observable.just(socket)
                    .map(Socket::getInputStream)
                    .map(ObjectInputStream::new)
                    .subscribe(inStream -> {
                        while(!emitter.isDisposed()) {
                            Object shape = inStream.readObject();
                            if (shape == null || socket.isClosed()) {
                                System.out.println("shape is null or socket disconnected");
                                emitter.onError(new ConnectException("socket error"));
                            }
                            else {
                                emitter.onNext((AbstractShape) shape);
                            }
                        }
                    }
                            , err -> {
                        System.out.println("Connection error or client disconnected");
                        System.err.println(err);
                        if (socket.isConnected()) socket.close();
                    });
        })
                .subscribeOn(Schedulers.io())
                .doOnNext(shape -> System.out.println("received " + shape.getClass().getName()))
                .subscribe(
                        shapeStream::onNext
                , err -> {
                            System.out.println("Error ?..");
                            System.err.println(err.getMessage());
                        }
                , () -> System.out.println("?"));


        // Subscribe the client to the shapeStream

        try {
            ObjectOutputStream objOutStream = new ObjectOutputStream(socket.getOutputStream());

            shapeStream
                    .subscribeOn(Schedulers.io())
                    .subscribe(objOutStream::writeObject
                    , err -> System.out.println("error sending shape to client: " +err)
                    , () -> System.out.println("shape sent to client"));


        } catch (Exception e) { e.printStackTrace(); }


        /*Observable.just(socket)
                .subscribeOn(Schedulers.io())
                .map(Socket::getInputStream)
                .map(ObjectInputStream::new)
                .flatMap(shape -> Observable.just(shape))
                //.doOnError(e -> e.printStackTrace())
                .subscribe(s -> {

                    while (true) {
                        Object obj = s.readObject();
                        if (obj == null || socket.isClosed()) {
                            System.out.println("connection lost");
                        }
                        System.out.println(obj.getClass().getName() + "on thread " + Thread.currentThread().getName());
                    }
                }
                ,Throwable::printStackTrace);

         */
        /*
        getIncomingShape(socket)
                .doOnNext(s -> System.out.println("received shape: " + s.getClass().getName()))
                .subscribe(shapeStream);

         */
        /*
        Observable<AbstractShape> shapeSource = Observable.create(emitter -> {
            getIncomingShape(socket)
                    .subscribe(shape -> {
                        while (!emitter.isDisposed()) {
                            emitter.onNext(shape);
                        }
                    });
        });

        shapeSource
                .subscribe(s -> System.out.println(s.getClass().getName()));

         */
    }



    private Observable<AbstractShape> getIncomingShape(Socket socket) {
        out.println("debug0, socket: " + socket.toString());
        return Observable.just(socket)
                .doOnNext(t -> out.println("debug1"))
                .map(Socket::getInputStream)
                .map(ObjectInputStream::new)
                .map(ObjectInputStream::readObject)
                .filter(Objects::nonNull)       // todo check..
                .map(aShape -> (AbstractShape) aShape);         // Cast to an AbstractShape
    }


}
