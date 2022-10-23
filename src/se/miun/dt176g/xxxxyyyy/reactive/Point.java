package se.miun.dt176g.xxxxyyyy.reactive;

import java.io.Serializable;

/**
 * Wrapper class to hold a X/Y position.
 * Must implement serializable for allowing sending over Socket.
 *
 * @author Rasmus Djupedal
 */
public class Point implements Serializable {
    private int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }

}
