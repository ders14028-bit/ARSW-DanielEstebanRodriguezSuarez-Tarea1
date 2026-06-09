package co.eci.matrix.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Board {
    private final int width;
    private final int height;
    private final Neo neo;
    private final List<Agent> agents;
    private final Set<Position> phones;
    private final Set<Position> obstacles;
    private volatile boolean neoEscaped = false;
    private volatile boolean neoCaught = false;
    private volatile boolean gameOverHandled = false;

    public enum MoveResult { MOVED, ESCAPED, CAUGHT }

    public Board(int width, int height, Neo neo, List<Agent> agents,
        List<Position> phonePositions, List<Position> obstaclePositions) {
        this.width = width;
        this.height = height;
        this.neo = neo;
        this.agents = agents;
        this.phones = new HashSet<>(phonePositions);
        this.obstacles = new HashSet<>(obstaclePositions);
    }

    public int width() { return width; }
    public int height() { return height; }
    public Neo neo() { return neo; }
    public List<Agent> agents() { return agents; }
    public synchronized Set<Position> phones() { return new HashSet<>(phones); }
    public synchronized Set<Position> obstacles() { return new HashSet<>(obstacles); }
    public boolean neoEscaped() { return neoEscaped; }
    public boolean neoCaught() { return neoCaught; }
    public boolean isOver() { return neoEscaped || neoCaught; }

    public synchronized boolean claimGameOver() {
        if (gameOverHandled) return false;
        gameOverHandled = true;
        return true;
    }

    public synchronized MoveResult moveNeo(int dx, int dy) {
        if (isOver()) return MoveResult.MOVED;
        Position next = new Position(
            Math.max(0, Math.min(width - 1, neo.position().x() + dx)),
            Math.max(0, Math.min(height - 1, neo.position().y() + dy))
);
        if (obstacles.contains(next)) return MoveResult.MOVED;
        neo.moveTo(next);

        if (phones.contains(next)) {
            neoEscaped = true;
            return MoveResult.ESCAPED;
        }
        for (Agent a : agents) {
            if (a.position().equals(next)) {
                neoCaught = true;
                return MoveResult.CAUGHT;
            }
        }
        return MoveResult.MOVED;
    }

    public synchronized MoveResult moveAgent(Agent agent) {
            if (isOver()) return MoveResult.MOVED;
        
            Position cur = agent.position();
            Position neoPos = neo.position();

         int dx = Integer.compare(neoPos.x(), cur.x());
            int dy = Integer.compare(neoPos.y(), cur.y());
            Position next = new Position(
                Math.max(0, Math.min(width - 1, cur.x() + dx)),
                Math.max(0, Math.min(height - 1, cur.y() + dy))
            );

       
            if (obstacles.contains(next) || phones.contains(next)) return MoveResult.MOVED;
            for (Agent other : agents) {
                if (other != agent && other.position().equals(next)) return MoveResult.MOVED;
            }

            agent.moveTo(next);

            if (next.equals(neo.position())) {
                neoCaught = true;
                return MoveResult.CAUGHT;
            }
        return MoveResult.MOVED;
    }

    private Position randomEmpty() {
        var rnd = ThreadLocalRandom.current();
        Position p;
        int guard = 0;
        do {
            p = new Position(rnd.nextInt(width), rnd.nextInt(height));
            guard++;
            if (guard > width * height * 2) break;
        } while (phones.contains(p) || obstacles.contains(p)
                || p.equals(neo.position()));
        return p;
    }
}