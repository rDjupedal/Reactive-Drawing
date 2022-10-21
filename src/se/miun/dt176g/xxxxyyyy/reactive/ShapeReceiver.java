package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.Socket;

public class ShapeReceiver {

    private Observable<AbstractShape> shapeObservable;

    public ShapeReceiver(Socket socket) {

        shapeObservable = Observable.create(emitter -> {

                    Observable.just(socket)
                            .map(Socket::getInputStream)
                            .map(ObjectInputStream::new)
                            .subscribe(inStream -> {
                                System.out.println("waiting for shapes on thread " + Thread.currentThread().getName());
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
                                System.out.println("disposed");

                            }

                            , err -> {
                                System.out.println(err.getMessage());
                                if (!emitter.isDisposed()) emitter.onError(new Throwable("Disconnected"));

                                if (socket.isConnected()) socket.close();
                            });
                });

    }


    protected Observable<AbstractShape> getObserver() {
        return shapeObservable;
    }

}
