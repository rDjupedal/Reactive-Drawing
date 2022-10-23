package se.miun.dt176g.xxxxyyyy.reactive;

import io.reactivex.rxjava3.schedulers.Schedulers;
import java.awt.*;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class TestClient {


    public static void main(String[] args) throws Exception{

        Socket socket = new Socket("localhost", 5000);

        ShapeReceiver shapeReceiver = new ShapeReceiver(socket);
        shapeReceiver.getObserver()
                .subscribeOn(Schedulers.io())
                .subscribe(
                        shape -> System.out.println("client received shape: " + shape.getClass().getName())
                        , err -> {
                            System.out.println("Error ?..");
                            System.err.println(err.getMessage());
                        }
                        , () -> System.out.println("?"));


        ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
        int shapeNo = 0;

        Random rnd = new Random();
        while (shapeNo++ < 1000) {
            int x = rnd.nextInt(500);
            int y = rnd.nextInt(500);
            int width = rnd.nextInt(200);
            int height = rnd.nextInt(200);
            int thick = rnd.nextInt(3) + 1;
            Color color = new Color((int) Math.random() * 0x1000000);

            RectangleShape rec = new RectangleShape(x, y, color, thick);
            rec.dragTo(new Point(x + width, y + height));

            System.out.println("Sending shape " + shapeNo + " x: " + x + ", y: " + y + " width: " + width + " height: " + height);
            objectOutput.writeObject(rec);

            Thread.sleep(3000);

        }

        objectOutput.close();
        socket.close();

    }


}
