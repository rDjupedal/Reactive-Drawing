package se.miun.dt176g.xxxxyyyy.reactive;

import java.awt.*;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TestServer {


    public static void main(String[] args) throws Exception{

        RectangleShape rec1 = new RectangleShape(100,100, new Color(Color.BLUE.getRGB()),1 );
        Socket con = new Socket("localhost", 5000);

        System.out.println("sending object1..");
        ObjectOutputStream objectOutput = new ObjectOutputStream(con.getOutputStream());
        objectOutput.writeObject(rec1);

        Thread.sleep(1000);
        RectangleShape rec2 = new RectangleShape(200,200, new Color(Color.BLUE.getRGB()),1 );
        System.out.println("sending object2..");
        objectOutput.writeObject(rec2);

        Thread.sleep(2000);


        objectOutput.close();
        con.close();

    }


}
