package com.yuda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CoordinatePicker extends JFrame {
    private List<Point> points = new ArrayList<>();
    private Image image;

    public CoordinatePicker(String imagePath) {
        image = new ImageIcon(imagePath).getImage();
        setTitle("Coordinate Picker");
        setSize(image.getWidth(null), image.getHeight(null));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
                g.setColor(Color.RED);
                for (Point p : points) {
                    g.fillOval(p.x - 3, p.y - 3, 6, 6);
                }
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                points.add(p);
                System.out.println("Point " + points.size() + ": (" + p.x + ", " + p.y + ")");
                repaint();
            }
        });

        add(panel);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CoordinatePicker("D:\\和著府-地下室平面图.jpg"));
    }
}
