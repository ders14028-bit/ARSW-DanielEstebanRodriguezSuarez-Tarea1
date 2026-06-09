package co.eci.matrix.concurrency;

import co.eci.matrix.core.Agent;
import co.eci.matrix.core.Board;
import co.eci.matrix.core.Position;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class NeoRunner implements Runnable {
    private final Board board;
    private final int sleepMs = 400;
    private final int dangerRadius = 4;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();

    public NeoRunner(Board board) {
        this.board = board;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !board.isOver()) {
                Position neo = board.neo().position();
                List<Agent> agents = board.agents();

                int[][] dirs = {
                    {-1,-1},{0,-1},{1,-1},
                    {-1, 0},       {1, 0},
                    {-1, 1},{0, 1},{1, 1}
                };

                Position closestPhone = null;
                int minPhoneDist = Integer.MAX_VALUE;
                for (Position p : board.phones()) {
                    int d = dist(neo, p);
                    if (d < minPhoneDist) {
                        minPhoneDist = d;
                        closestPhone = p;
                    }
                }

                int bestScore = Integer.MIN_VALUE;
                int[] bestDir = dirs[ThreadLocalRandom.current().nextInt(dirs.length)];

                for (int[] dir : dirs) {

                    Position next = new Position(
                        Math.max(0, Math.min(board.width() - 1, neo.x() + dir[0])),
                        Math.max(0, Math.min(board.height() - 1, neo.y() + dir[1]))
                    );

                    if (board.obstacles().contains(next)) continue;

                    if (closestPhone != null && next.equals(closestPhone)) {
                        bestDir = dir;
                        break;
                    }

                    int minDistToAgent = Integer.MAX_VALUE;
                    for (Agent a : agents) {
                        int d = dist(next, a.position());
                        if (d < minDistToAgent) minDistToAgent = d;
                    }

                    int distToPhone = closestPhone != null ? dist(next, closestPhone) : 0;
                    int currentDistToPhone = closestPhone != null ? dist(neo, closestPhone) : 0;

                    if (currentDistToPhone <= 3) {
                        int score = -distToPhone * 100;
                        if (score > bestScore) {
                            bestScore = score;
                            bestDir = dir;
                        }
                        continue;
                    }

                    int score;
                    if (minDistToAgent <= dangerRadius) {
                        score = minDistToAgent * 10 - distToPhone;
                    } else {
                        score = -distToPhone * 10 + minDistToAgent;
                    }

                    if (score > bestScore) {
                        bestScore = score;
                        bestDir = dir;
                    }
                }

                board.moveNeo(bestDir[0], bestDir[1]);

                synchronized (pauseLock) {
                    while (paused) pauseLock.wait();
                }
                Thread.sleep(sleepMs);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int dist(Position a, Position b) {
        return Math.abs(a.x() - b.x()) + Math.abs(a.y() - b.y());
    }

    public void pause() { paused = true; }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
}