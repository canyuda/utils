package com.yuda;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CanvasPanel extends JPanel {
    private final Image backgroundImage;
    private final List<List<Point>> savedPolygons = new ArrayList<>();
    private final List<String> polygonNames = new ArrayList<>();
    private final List<Point> currentPoints = new ArrayList<>();

    private Point dragStartPoint;
    private Point viewStartPosition;

    public CanvasPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setPreferredSize(new Dimension(backgroundImage.getWidth(this), backgroundImage.getHeight(this)));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this);

        drawSavedPolygons(g);
        drawCurrentPolygon(g);
    }

    private void drawSavedPolygons(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        setupGraphics(g2);

        for (int i = 0; i < savedPolygons.size(); i++) {
            drawPolygon(g2, savedPolygons.get(i), false, polygonNames.get(i));
        }
    }

    private void drawCurrentPolygon(Graphics g) {
        if (currentPoints.isEmpty()) return;

        Graphics2D g2 = (Graphics2D) g;
        setupGraphics(g2);

        List<Point> orderedPoints = orderCurrentPoints(currentPoints);
        drawPolygon(g2, orderedPoints, true, "");
    }

    private void setupGraphics(Graphics2D g2) {
        g2.setStroke(new BasicStroke(Constants.STROKE_WIDTH));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void drawPolygon(Graphics2D g2, List<Point> points, boolean isCurrent, String name) {
        drawPoints(g2, points, isCurrent);
        drawLines(g2, points);

        if (points.size() >= 3) {
            drawClosedPolygon(g2, points);
        }

        if (name != null && !name.isEmpty()) {
            drawPolygonName(g2, points, name);
        }
    }

    private void drawPoints(Graphics2D g2, List<Point> points, boolean isCurrent) {
        Color pointColor = isCurrent ? Constants.ACCENT_COLOR : Constants.SECONDARY_COLOR;
        g2.setColor(pointColor);

        int pointSizeHalf = Constants.POINT_SIZE / 2;
        for (Point point : points) {
            g2.fillOval(point.x - pointSizeHalf, point.y - pointSizeHalf,
                    Constants.POINT_SIZE, Constants.POINT_SIZE);

            g2.setColor(Color.WHITE);
            g2.drawOval(point.x - pointSizeHalf, point.y - pointSizeHalf,
                    Constants.POINT_SIZE, Constants.POINT_SIZE);
            g2.setColor(pointColor);
        }
    }

    private void drawLines(Graphics2D g2, List<Point> points) {
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i - 1);
            Point p2 = points.get(i);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawClosedPolygon(Graphics2D g2, List<Point> points) {
        Polygon polygon = new Polygon();
        points.forEach(pt -> polygon.addPoint(pt.x, pt.y));
        g2.draw(polygon);
    }

    private void drawPolygonName(Graphics2D g2, List<Point> points, String name) {
        int centerX = calculateCenterX(points);
        int centerY = calculateCenterY(points);

        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int textHeight = fm.getHeight();

        drawTextBackground(g2, centerX, centerY, textWidth, textHeight);
        drawText(g2, centerX, centerY, name, fm, textHeight);
    }

    private int calculateCenterX(List<Point> points) {
        return points.stream().mapToInt(p -> (int) p.getX()).sum() / points.size();
    }

    private int calculateCenterY(List<Point> points) {
        return points.stream().mapToInt(p -> (int) p.getY()).sum() / points.size();
    }

    private void drawTextBackground(Graphics2D g2, int centerX, int centerY, int textWidth, int textHeight) {
        int x = centerX - textWidth / 2 - Constants.TEXT_BACKGROUND_PADDING;
        int y = centerY - textHeight / 2 - Constants.TEXT_BACKGROUND_PADDING;
        int width = textWidth + Constants.TEXT_BACKGROUND_PADDING * 2;
        int height = textHeight + Constants.TEXT_BACKGROUND_PADDING * 2;

        g2.setColor(Constants.WHITE_SEMI_TRANSPARENT);
        g2.fillRoundRect(x, y, width, height,
                Constants.TEXT_BACKGROUND_ROUND, Constants.TEXT_BACKGROUND_ROUND);
    }

    private void drawText(Graphics2D g2, int centerX, int centerY, String name, FontMetrics fm, int textHeight) {
        g2.setColor(Constants.TEXT_COLOR);
        g2.setFont(Constants.LABEL_FONT);
        g2.drawString(name, centerX - fm.stringWidth(name) / 2,
                centerY + textHeight / 2 - fm.getDescent());
    }

    private List<Point> orderCurrentPoints(List<Point> points) {
        int pointCount = points.size();
        if (pointCount < 2) return new ArrayList<>(points);

        List<Point> orderedPoints = new ArrayList<>(points);
        orderedPoints.sort(Comparator.comparingInt((Point p) -> (int) p.getY()).thenComparingInt((Point p) -> (int) p.getX()));

        if (pointCount == 2) return orderedPoints;
        if (pointCount == 4) return orderFourPoints(orderedPoints);

        return orderedPoints;
    }

    private List<Point> orderFourPoints(List<Point> points) {
        Point topLeft = points.get(0).x < points.get(1).x ? points.get(0) : points.get(1);
        Point topRight = points.get(0).x > points.get(1).x ? points.get(0) : points.get(1);

        Point bottomLeft = points.get(2).x < points.get(3).x ? points.get(2) : points.get(3);
        Point bottomRight = points.get(2).x > points.get(3).x ? points.get(2) : points.get(3);

        List<Point> orderedPoints = new ArrayList<>();
        orderedPoints.add(topLeft);
        orderedPoints.add(topRight);
        orderedPoints.add(bottomRight);
        orderedPoints.add(bottomLeft);

        return orderedPoints;
    }

    public List<Point> orderPointsForSaving(List<Point> points) {
        if (points.size() != 4) return new ArrayList<>(points);

        int centerX = calculateCenterX(points);
        int centerY = calculateCenterY(points);

        List<Point> orderedPoints = new ArrayList<>(points);
        final int cx = centerX, cy = centerY;

        orderedPoints.sort((a, b) -> {
            double angleA = Math.atan2(a.y - cy, a.x - cx);
            double angleB = Math.atan2(b.y - cy, b.x - cx);
            return Double.compare(angleB, angleA); // 顺时针排序
        });

        return orderedPoints;
    }

    // Getters and setters
    public List<Point> getCurrentPoints() {
        return currentPoints;
    }

    public void addCurrentPoint(Point point) {
        currentPoints.add(point);
        repaint();
    }

    public void updateLastCurrentPoint(Point point) {
        if (!currentPoints.isEmpty()) {
            currentPoints.set(currentPoints.size() - 1, point);
            repaint();
        }
    }

    public void removeLastCurrentPoint() {
        if (!currentPoints.isEmpty()) {
            currentPoints.remove(currentPoints.size() - 1);
            repaint();
        }
    }

    public void clearCurrentPoints() {
        currentPoints.clear();
        repaint();
    }

    public void addSavedPolygon(List<Point> polygon, String name) {
        savedPolygons.add(new ArrayList<>(polygon));
        polygonNames.add(name);
        repaint();
    }

    public List<List<Point>> getSavedPolygons() {
        return savedPolygons;
    }

    public List<String> getPolygonNames() {
        return polygonNames;
    }

    public Point getDragStartPoint() {
        return dragStartPoint;
    }

    public void setDragStartPoint(Point dragStartPoint) {
        this.dragStartPoint = dragStartPoint;
    }

    public Point getViewStartPosition() {
        return viewStartPosition;
    }

    public void setViewStartPosition(Point viewStartPosition) {
        this.viewStartPosition = viewStartPosition;
    }
}