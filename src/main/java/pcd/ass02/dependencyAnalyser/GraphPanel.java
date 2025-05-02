package pcd.ass02.dependencyAnalyser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {
    public static final int NODE_SIZE = 10;
    public static final int LABEL_OFFSET = 20;
    private final Map<String, Point> nodes = new HashMap<>();
    private final List<String[]> edges = new ArrayList<>();
    private String dragging = null;

    public GraphPanel() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                for (var entry : nodes.entrySet()) {
                    Point p = entry.getValue();
                    if (e.getPoint().distance(p) < (double) NODE_SIZE / 2) {
                        dragging = entry.getKey();
                        break;
                    }
                }
            }
            public void mouseReleased(MouseEvent e) {
                dragging = null;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragging != null) {
                    nodes.get(dragging).setLocation(e.getPoint());
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        synchronized (nodes) {
            for (var edge : edges) {
                Point p1 = nodes.get(edge[0]);
                Point p2 = nodes.get(edge[1]);
                g2.setColor(Color.GRAY);
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            for (var entry : nodes.entrySet()) {
                Point p = entry.getValue();
                g2.setColor(Color.BLACK);
                g2.fillOval(p.x - (NODE_SIZE / 2), p.y - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);
                g2.drawString(entry.getKey(), p.x - LABEL_OFFSET, p.y + LABEL_OFFSET);
            }
        }
    }

    private Point getEmptyPoint() {
        Random random = new Random();
        Point point = new Point(random.nextInt(getWidth()), random.nextInt(getHeight()));
        return nodes.values().stream().anyMatch(p -> p.distance(point) < NODE_SIZE) ? getEmptyPoint() : point;
    }

    public void add(String node, Set<String> dependencies) {
        synchronized (nodes) {
            if (!this.nodes.containsKey(node)) {
                this.nodes.put(node, this.getEmptyPoint());
            }
            for (String dep : dependencies) {
                if (!this.nodes.containsKey(dep)) {
                    this.nodes.put(dep, this.getEmptyPoint());
                }
                this.edges.add(new String[]{node, dep});
            }
        }
        repaint();
    }

}