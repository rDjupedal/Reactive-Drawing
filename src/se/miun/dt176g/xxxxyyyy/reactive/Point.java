package se.miun.dt176g.xxxxyyyy.reactive;

import java.io.Serializable;

public class Point implements Serializable {
    private int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }

    public void print() {
        System.out.println("X: " + this.x + " Y: " + this.y);
    }
}
