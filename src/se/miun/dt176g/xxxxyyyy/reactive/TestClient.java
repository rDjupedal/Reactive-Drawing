package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;

public class TestClient {


    public static void main(String[] args) throws Exception{

        Socket socket = new Socket("localhost", 5000);

        ShapeReceiver shapeReceiver = new ShapeReceiver(socket);
        shapeReceiver.getObserver()
                .subscribeOn(Schedulers.io())
                //.doOnNext(shape -> System.out.println("received " + shape.getClass().getName()))
                .subscribe(
                        //shapeStream::onNext
                        shape -> System.out.println("client received shape: " + shape.getClass().getName())
                        , err -> {
                            System.out.println("Error ?..");
                            System.err.println(err.getMessage());
                        }
                        , () -> System.out.println("?"));

        /*
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
                //.doOnNext(shape -> System.out.println("received " + shape.getClass().getName()))
                .subscribe(
                        //shapeStream::onNext
                        shape -> System.out.println("client received shape: " + shape.getClass().getName())
                        , err -> {
                            System.out.println("Error ?..");
                            System.err.println(err.getMessage());
                        }
                        , () -> System.out.println("?"));

         */



        RectangleShape rec1 = new RectangleShape(100,100, new Color(Color.BLUE.getRGB()),1 );
        rec1.dragTo(new Point(150,150));
        System.out.println("sending object1..");
        ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
        objectOutput.writeObject(rec1);

        Thread.sleep(1000);
        RectangleShape rec2 = new RectangleShape(200,200, new Color(Color.BLUE.getRGB()),1 );
        rec2.dragTo(new Point(300,300));
        System.out.println("sending object2..");
        objectOutput.writeObject(rec2);

        Thread.sleep(30000);


        objectOutput.close();
        socket.close();

    }


}
