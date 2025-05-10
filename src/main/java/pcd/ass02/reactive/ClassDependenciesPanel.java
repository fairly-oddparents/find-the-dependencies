package pcd.ass02.reactive;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ClassDependenciesPanel extends JPanel {

    private static final int NODE_SIZE = 20;
    private static final int TITLE_HEIGHT = 30;
    private static final int BLOCK_SIZE = 300;

    private static class Node {
        final String name;
        final String pkg;
        final Point pos = new Point();
        final Set<Node> dependencies = new HashSet<>();

        Node(String name) {
            this.name = name;
            this.pkg = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : "";
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, Color> packageColors = new HashMap<>();
    private final Random rand = new Random();
    private double scale = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastMouse = null;

    public ClassDependenciesPanel() {
        setBackground(Color.WHITE);

        addMouseWheelListener(e -> {
            scale *= e.getWheelRotation() < 0 ? 1.1 : 0.9;
            repaint();
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                lastMouse = e.getPoint();
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent e) {
                Point p = e.getPoint();
                offsetX += (p.x - lastMouse.x);
                offsetY += (p.y - lastMouse.y);
                lastMouse = p;
                repaint();
            }
        });
    }

    public void add(String className, Set<String> dependencies) {
        synchronized (nodes) {
            Node node = nodes.computeIfAbsent(className, Node::new);
            for (String dep : dependencies) {
                Node depNode = nodes.computeIfAbsent(dep, Node::new);
                node.dependencies.add(depNode);
            }
        }
        applyLayout();
        repaint();
    }

    public void clear() {
        synchronized (nodes) {
        nodes.clear();
        packageColors.clear();
        scale = 1.0;
        offsetX = offsetY = 0;
        }
        repaint();
    }

    private void applyLayout() {
        Map<String, List<Node>> pkgMap = new HashMap<>();
        for (Node node : nodes.values()) {
            pkgMap.computeIfAbsent(node.pkg, k -> new ArrayList<>()).add(node);
        }

        int pkgCount = pkgMap.size();
        int cols = (int) Math.ceil(Math.sqrt(pkgCount));
        int rows = (int) Math.ceil((double) pkgCount / cols);
        int i = 0;

        for (Map.Entry<String, List<Node>> entry : pkgMap.entrySet()) {
            int bx = (i % cols) * BLOCK_SIZE;
            int by = (i / cols) * BLOCK_SIZE;
            List<Node> group = entry.getValue();

            arrangeNodes(group, bx, by);
            i++;
        }

        int totalWidth = cols * BLOCK_SIZE;
        int totalHeight = rows * BLOCK_SIZE;
        setPreferredSize(new Dimension(totalWidth, totalHeight));
        revalidate();
    }

    private void arrangeNodes(List<Node> group, int bx, int by) {
        int spacingY = (BLOCK_SIZE - TITLE_HEIGHT) / (group.size() + 1);
        int centerX = bx + BLOCK_SIZE / 2;

        for (int j = 0; j < group.size(); j++) {
            Node node = group.get(j);
            node.pos.x = centerX;
            node.pos.y = by + TITLE_HEIGHT + spacingY * (j + 1);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        synchronized (nodes) {
            Map<String, List<Node>> packages = groupByPackage();
            drawPackages(g, packages);
            drawEdges(g);
            drawNodes(g);
        }
    }

    private Map<String, List<Node>> groupByPackage() {
        Map<String, List<Node>> packages = new HashMap<>();
        for (Node node : nodes.values()) {
            packages.computeIfAbsent(node.pkg, k -> new ArrayList<>()).add(node);
            packageColors.computeIfAbsent(node.pkg, k -> new Color(
                    rand.nextInt(200),
                    rand.nextInt(200),
                    rand.nextInt(200), 50)
            );
        }
        return packages;
    }

    private void drawPackages(Graphics g, Map<String, List<Node>> packages) {
        for (Map.Entry<String, List<Node>> entry : packages.entrySet()) {
            List<Node> group = entry.getValue();
            if (group.isEmpty()) continue;
            drawPackage(g, entry.getKey(), group);
        }
    }

    private void drawPackage(Graphics g, String packageName, List<Node> group) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Node node : group) {
            minX = Math.min(minX, node.pos.x);
            minY = Math.min(minY, node.pos.y);
            maxX = Math.max(maxX, node.pos.x);
            maxY = Math.max(maxY, node.pos.y);
        }

        g.setColor(packageColors.get(packageName));
        g.fillRoundRect(
                minX - 40, minY - 40,
                (maxX - minX) + 80, (maxY - minY) + 80,
                NODE_SIZE, NODE_SIZE
        );

        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(packageName);
        int centerX = minX + (maxX - minX) / 2;
        g.setColor(Color.DARK_GRAY);
        g.drawString(packageName, centerX - textWidth / 2, minY - 45);
    }

    private void drawEdges(Graphics g) {
        for (Node node : nodes.values()) {
            for (Node dep : node.dependencies) {
                g.setColor(Color.GRAY);
                drawArrow(g, node.pos.x, node.pos.y, dep.pos.x, dep.pos.y);
            }
        }
    }

    private void drawNodes(Graphics g) {
        for (Node node : nodes.values()) {
            g.setColor(Color.ORANGE);
            g.fillOval(node.pos.x - NODE_SIZE / 2, node.pos.y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE);
            g.setColor(Color.BLACK);
            g.drawString(
                    node.name.substring(node.name.lastIndexOf('.') + 1),
                    node.pos.x + 15,
                    node.pos.y + 5
            );
        }
    }

    private void drawArrow(Graphics g, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1, dy = y2 - y1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) return;

        double ux = dx / dist, uy = dy / dist;
        int tx2 = x2 - (int)(ux * NODE_SIZE / 2);
        int ty2 = y2 - (int)(uy * NODE_SIZE / 2);
        int tx1 = x1 + (int)(ux * NODE_SIZE / 2);
        int ty1 = y1 + (int)(uy * NODE_SIZE / 2);

        g.drawLine(tx1, ty1, tx2, ty2);

        int len = 10;
        double angle = Math.atan2(ty2 - ty1, tx2 - tx1);
        int ax1 = (int)(tx2 - len * Math.cos(angle - Math.PI / 6));
        int ay1 = (int)(ty2 - len * Math.sin(angle - Math.PI / 6));
        int ax2 = (int)(tx2 - len * Math.cos(angle + Math.PI / 6));
        int ay2 = (int)(ty2 - len * Math.sin(angle + Math.PI / 6));
        g.drawLine(tx2, ty2, ax1, ay1);
        g.drawLine(tx2, ty2, ax2, ay2);
    }
}