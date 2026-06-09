package co.eci.matrix.concurrency;

import co.eci.matrix.core.Agent;
import co.eci.matrix.core.Board;

public final class AgentRunner implements Runnable {
    private final Agent agent;
    private final Board board;
    private final int sleepMs = 700;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public AgentRunner(Agent agent, Board board) {
        this.agent = agent;
        this.board = board;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !board.isOver()) {
                board.moveAgent(agent);

                synchronized (pauseLock) {
                    while (paused) pauseLock.wait();
                }
                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void pause() { paused = true; }
    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
}
