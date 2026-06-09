package co.eci.matrix.ui;

import co.eci.matrix.core.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Setup extends JFrame {

    public enum Tool { NEO, AGENT, PHONE, OBSTACLE, ERASE }

    private final int boardSize;
    private final int cell = 24;

    private Position neoPos = null;
    private final List<Position> agents = new ArrayList<>();
    private final Set<Position> phones = new HashSet<>();
    private final Set<Position> obstacles = new HashSet<>();

    private Tool selectedTool = Tool.NEO;

    public Setup(int boardSize) {
        super("Matrix Setup");
        this.boardSize = boardSize;

        SetupPanel setupPanel = new SetupPanel();
        JPanel toolbar = new JPanel();
        JButton neoBtn      = new JButton("Neo");
        JButton agentBtn    = new JButton("Agent");
        JButton phoneBtn    = new JButton("Phone");
        JButton obstacleBtn = new JButton("Obstacle");
        JButton eraseBtn    = new JButton("Erase");
        JButton startBtn    = new JButton("Start Game");

        neoBtn.setBackground(new Color(0, 200, 0));
        agentBtn.setBackground(Color.RED);
        phoneBtn.setBackground(new Color(255, 210, 0));
        obstacleBtn.setBackground(new Color(30, 100, 220));
        eraseBtn.setBackground(Color.LIGHT_GRAY);
        startBtn.setBackground(Color.BLACK);
        startBtn.setForeground(Color.WHITE);

        toolbar.add(neoBtn);
        toolbar.add(agentBtn);
        toolbar.add(phoneBtn);
        toolbar.add(obstacleBtn);
        toolbar.add(eraseBtn);
        toolbar.add(startBtn);

        neoBtn.addActionListener(e -> selectedTool = Tool.NEO);
        agentBtn.addActionListener(e -> selectedTool = Tool.AGENT);
        phoneBtn.addActionListener(e -> selectedTool = Tool.PHONE);
        obstacleBtn.addActionListener(e -> selectedTool = Tool.OBSTACLE);
        eraseBtn.addActionListener(e -> selectedTool = Tool.ERASE);

        startBtn.addActionListener(e -> {
            if (neoPos == null) {
                JOptionPane.showMessageDialog(this, "Place Neo first!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (agents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Place at least one agent!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (phones.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Place at least one phone!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dispose();
            MatrixApp.launch(boardSize, neoPos, new ArrayList<>(agents),
                    new ArrayList<>(phones), new ArrayList<>(obstacles));
        });

        setupPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX() / cell;
                int y = e.getY() / cell;
                if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return;
                Position p = new Position(x, y);

                switch (selectedTool) {
                    
                    case AGENT -> {
                        if (!agents.contains(p) && !p.equals(neoPos) && !phones.contains(p) && !obstacles.contains(p))
                            agents.add(p);
                    }
                    case NEO -> {
                        if (!agents.contains(p) && !phones.contains(p) && !obstacles.contains(p))
                            neoPos = p;
                    }
                    case PHONE -> {
                        if (!agents.contains(p) && !p.equals(neoPos) && !obstacles.contains(p))
                            phones.add(p);
                    }
                    case OBSTACLE -> {
                        if (!agents.contains(p) && !p.equals(neoPos) && !phones.contains(p))
                            obstacles.add(p);
                    }
                    
                    case ERASE -> {
                        if (p.equals(neoPos)) neoPos = null;
                        agents.remove(p);
                        phones.remove(p);
                        obstacles.remove(p);
                    }
                }
                setupPanel.repaint();
            }
        });

        setLayout(new BorderLayout());
        add(setupPanel, BorderLayout.CENTER);
        add(toolbar, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private final class SetupPanel extends JPanel {
        public SetupPanel() {
            setPreferredSize(new Dimension(boardSize * cell + 1, boardSize * cell + 1));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Grid
            g2.setColor(Color.BLACK);
            for (int x = 0; x <= boardSize; x++)
                g2.drawLine(x * cell, 0, x * cell, boardSize * cell);
            for (int y = 0; y <= boardSize; y++)
                g2.drawLine(0, y * cell, boardSize * cell, y * cell);

            // Obstacles
            g2.setColor(new Color(30, 100, 220));
            for (var p : obstacles)
                g2.fillRect(p.x() * cell + 2, p.y() * cell + 2, cell - 4, cell - 4);

            // Phones
            g2.setColor(new Color(255, 210, 0));
            for (var p : phones) {
                int x = p.x() * cell, y = p.y() * cell;
                int[] xs = { x + cell / 2, x + cell - 3, x + 3 };
                int[] ys = { y + 3, y + cell - 3, y + cell - 3 };
                g2.fillPolygon(xs, ys, 3);
            }

            // Agents
            g2.setColor(Color.RED);
            for (var p : agents)
                g2.fillOval(p.x() * cell + 3, p.y() * cell + 3, cell - 6, cell - 6);

            // Neo
            if (neoPos != null) {
                g2.setColor(new Color(0, 200, 0));
                g2.fillOval(neoPos.x() * cell + 3, neoPos.y() * cell + 3, cell - 6, cell - 6);
            }

            g2.dispose();
        }
    }

    public static void launch(int boardSize) {
        SwingUtilities.invokeLater(() -> new Setup(boardSize));
    }
}
