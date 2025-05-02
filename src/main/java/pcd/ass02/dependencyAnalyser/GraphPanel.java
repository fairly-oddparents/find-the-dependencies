package pcd.ass02.dependencyAnalyser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

public class GraphPanel extends JPanel {
    public static final int NODE_SIZE = 10;
    public static final int LABEL_OFFSET = NODE_SIZE + 10;
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

    private Point offsetPoint(Point from, Point to) {
        double angle = Math.atan2(to.y - from.y, to.x - from.x);
        return new Point(
                (int) (to.x - Math.cos(angle) * NODE_SIZE / 2),
                (int) (to.y - Math.sin(angle) * NODE_SIZE / 2)
        );
    }

    private void drawArrow(Graphics2D g2, Point from, Point to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.hypot(dx, dy);

        AffineTransform old = g2.getTransform();
        g2.translate(from.x, from.y);
        g2.rotate(angle);
        g2.drawLine(0, 0, len, 0);
        int arrowSize = NODE_SIZE / 2;
        g2.fillPolygon(
                new int[]{len, len - arrowSize, len - arrowSize, len},
                new int[]{0, -arrowSize, arrowSize, 0},
                4
        );
        g2.setTransform(old);
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
                drawArrow(g2, this.offsetPoint(p1, p2), this.offsetPoint(p2, p1));
            }

            for (var entry : nodes.entrySet()) {
                Point p = entry.getValue();
                g2.setColor(Color.BLACK);
                g2.fillOval(p.x - (NODE_SIZE / 2), p.y - (NODE_SIZE / 2), NODE_SIZE, NODE_SIZE);
                g2.drawString(entry.getKey(), p.x, p.y + LABEL_OFFSET);
            }
        }
    }

    private Point getEmptyPoint() {
        Random random = new Random();
        int x = random.nextInt(this.getWidth() - (2 * NODE_SIZE)) + NODE_SIZE;
        int y = random.nextInt(this.getHeight() - (2 * NODE_SIZE) - LABEL_OFFSET) + NODE_SIZE;
        Point point = new Point(x, y);
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

    public void clear() {
        synchronized (nodes) {
            nodes.clear();
            edges.clear();
        }
        repaint();
    }

}