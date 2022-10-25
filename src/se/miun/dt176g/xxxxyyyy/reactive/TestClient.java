package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Used for sending random shapes to the server while debugging..
 *
 * @author Rasmus Djupedal
 */
public class TestClient {

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 5000);

        ShapeReceiver shapeReceiver = new ShapeReceiver(socket);
        shapeReceiver.getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        shape -> System.out.println("client received shape: " + shape.getClass().getName())
                        , err -> System.err.println(err.getMessage()));

        ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
        Random rnd = new Random();
        Observable.interval(2, TimeUnit.MILLISECONDS)
                .doOnNext(System.out::println)
                .map( t -> new RectangleShape(
                        (new Point(rnd.nextInt(500),
                        rnd.nextInt(500))),
                        (new Color((int)(Math.random() * 0x1000000))),
                        rnd.nextInt(24) + 1))
                .doOnNext(rec -> System.out.println("Sending rectangle"))
                .map(rectangle -> {
                    rectangle.dragTo(new Point(rnd.nextInt(500), rnd.nextInt(500)));
                    return rectangle;
                })
                .blockingSubscribe(objectOutput::writeObject);

        objectOutput.close();
        socket.close();
    }

}
