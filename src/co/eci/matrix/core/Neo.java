package co.eci.matrix.core;

public final class Neo {
    private volatile Position position;

    public Neo(Position start) {
        this.position = start;
    }

    public Position position() {
        return position;
    }

    public synchronized void moveTo(Position next) {
        this.position = next;
    }
}
