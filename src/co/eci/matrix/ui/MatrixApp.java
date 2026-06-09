package co.eci.matrix.ui;

import co.eci.matrix.concurrency.AgentRunner;
import co.eci.matrix.concurrency.NeoRunner;
import co.eci.matrix.core.Agent;
import co.eci.matrix.core.Board;
import co.eci.matrix.core.Neo;
import co.eci.matrix.core.Position;
import co.eci.matrix.core.engine.GameClock;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public final class MatrixApp extends JFrame {

    private final Board board;
    private final GamePanel gamePanel;
    private GameClock clock;

    public MatrixApp(int boardSize, Position neoStart, List<Position> agentPositions,
                     List<Position> phonePositions, List<Position> obstaclePositions) {
        super("The Matrix");

        Neo neo = new Neo(neoStart);
        List<Agent> agents = new ArrayList<>();
        for (Position p : agentPositions) agents.add(new Agent(p));

        this.board = new Board(boardSize, boardSize, neo, agents, phonePositions, obstaclePositions);
        this.gamePanel = new GamePanel(board);

        JButton pauseButton = new JButton("Pause");
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(pauseButton, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        this.clock = new GameClock(60, () -> SwingUtilities.invokeLater(() -> {
            gamePanel.repaint();
            if (board.isOver() && board.claimGameOver()) {
                clock.stop();
                String msg = board.neoEscaped() ? "Neo escaped! You win!" : "Neo was caught! Game Over.";
                JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }
        }));

        var exec = Executors.newVirtualThreadPerTaskExecutor();
        NeoRunner neoRunner = new NeoRunner(board);
        List<AgentRunner> agentRunners = new ArrayList<>();
        exec.submit(neoRunner);
        for (Agent agent : agents) {
            AgentRunner ar = new AgentRunner(agent, board);
            agentRunners.add(ar);
            exec.submit(ar);
        }

        pauseButton.addActionListener(e -> {
            if ("Pause".equals(pauseButton.getText())) {
                pauseButton.setText("Resume");
                neoRunner.pause();
                agentRunners.forEach(AgentRunner::pause);
                clock.pause();
            } else {
                pauseButton.setText("Pause");
                neoRunner.resume();
                agentRunners.forEach(AgentRunner::resume);
                clock.resume();
            }
        });

        setVisible(true);
        clock.start();
    }

    public static final class GamePanel extends JPanel {
        private final Board board;
        private final int cell = 24;

        public GamePanel(Board board) {
            this.board = board;
            setPreferredSize(new Dimension(board.width() * cell + 1, board.height() * cell + 1));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Grid
            g2.setColor(Color.BLACK);
            for (int x = 0; x <= board.width(); x++)
                g2.drawLine(x * cell, 0, x * cell, board.height() * cell);
            for (int y = 0; y <= board.height(); y++)
                g2.drawLine(0, y * cell, board.width() * cell, y * cell);

            // Obstacles - blue squares
            g2.setColor(new Color(30, 100, 220));
            for (var p : board.obstacles())
                g2.fillRect(p.x() * cell + 2, p.y() * cell + 2, cell - 4, cell - 4);

            // Phones - yellow triangles
            g2.setColor(new Color(255, 210, 0));
            for (var p : board.phones()) {
                int x = p.x() * cell, y = p.y() * cell;
                int[] xs = { x + cell / 2, x + cell - 3, x + 3 };
                int[] ys = { y + 3, y + cell - 3, y + cell - 3 };
                g2.fillPolygon(xs, ys, 3);
            }

            // Agents - red circles
            g2.setColor(Color.RED);
            for (var agent : board.agents()) {
                var p = agent.position();
                g2.fillOval(p.x() * cell + 3, p.y() * cell + 3, cell - 6, cell - 6);
            }

            // Neo - green circle
            g2.setColor(new Color(0, 200, 0));
            var np = board.neo().position();
            g2.fillOval(np.x() * cell + 3, np.y() * cell + 3, cell - 6, cell - 6);

            g2.dispose();
        }
    }

    public static void launch(int boardSize, Position neoStart, List<Position> agents,
                              List<Position> phones, List<Position> obstacles) {
        SwingUtilities.invokeLater(() -> new MatrixApp(boardSize, neoStart, agents, phones, obstacles));
    }
}