package co.eci.matrix.core;

public final class Agent {
    private volatile Position position;

    public Agent(Position start) {
        this.position = start;
    }

    public Position position() {
        return position;
    }

    public synchronized void moveTo(Position next) {
        this.position = next;
    }
}

