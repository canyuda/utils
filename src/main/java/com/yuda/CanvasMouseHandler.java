package com.yuda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class CanvasMouseHandler {
    private final CanvasPanel canvasPanel;
    private final JScrollPane scrollPane;

    public CanvasMouseHandler(CanvasPanel canvasPanel, JScrollPane scrollPane) {
        this.canvasPanel = canvasPanel;
        this.scrollPane = scrollPane;

        setupMouseListeners();
    }

    private void setupMouseListeners() {
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePress(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseRelease(e);
            }
        });

        canvasPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDrag(e);
            }
        });
    }

    private void handleMousePress(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            handleRightMousePress(e);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            handleLeftMousePress(e);
        }
    }

    private void handleRightMousePress(MouseEvent e) {
        canvasPanel.setDragStartPoint(e.getPoint());

        JViewport viewport = scrollPane.getViewport();
        if (viewport != null) {
            canvasPanel.setViewStartPosition(viewport.getViewPosition());
        }
    }

    private void handleLeftMousePress(MouseEvent e) {
        if (canvasPanel.getCurrentPoints().size() < 4) {
            canvasPanel.addCurrentPoint(e.getPoint());
        } else {
            JOptionPane.showMessageDialog(canvasPanel, Constants.MAX_POINTS_MSG);
        }
    }

    private void handleMouseClick(MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            canvasPanel.removeLastCurrentPoint();
        }
    }

    private void handleMouseRelease(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            canvasPanel.setDragStartPoint(null);
            canvasPanel.setViewStartPosition(null);
        }
    }

    private void handleMouseDrag(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            handleRightMouseDrag(e);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            handleLeftMouseDrag(e);
        }
    }

    private void handleRightMouseDrag(MouseEvent e) {
        Point dragStart = canvasPanel.getDragStartPoint();
        Point viewStart = canvasPanel.getViewStartPosition();

        if (dragStart != null && viewStart != null) {
            JViewport viewport = scrollPane.getViewport();
            if (viewport != null) {
                int dx = dragStart.x - e.getX();
                int dy = dragStart.y - e.getY();

                int newX = Math.max(0, Math.min(
                        viewStart.x + dx,
                        canvasPanel.getWidth() - viewport.getWidth()
                ));

                int newY = Math.max(0, Math.min(
                        viewStart.y + dy,
                        canvasPanel.getHeight() - viewport.getHeight()
                ));

                viewport.setViewPosition(new Point(newX, newY));
            }
        }
    }

    private void handleLeftMouseDrag(MouseEvent e) {
        if (canvasPanel.getCurrentPoints().size() < 4 && !canvasPanel.getCurrentPoints().isEmpty()) {
            canvasPanel.updateLastCurrentPoint(e.getPoint());
        }
    }
}