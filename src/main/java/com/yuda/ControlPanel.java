package com.yuda;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlPanel extends JPanel {
    private final JTextField nameField;
    private final JButton saveButton;
    private final JButton undoLastButton;
    private final JButton undoAllButton;
    private final JButton closeButton;

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Constants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Constants.CONTROL_PANEL_PADDING,
                Constants.CONTROL_PANEL_PADDING,
                Constants.CONTROL_PANEL_PADDING,
                Constants.CONTROL_PANEL_PADDING
        ));

        JPanel firstRow = createButtonRow();
        JPanel secondRow = createActionRow();

        add(firstRow);
        add(Box.createVerticalStrut(Constants.CONTROL_PANEL_PADDING));
        add(secondRow);

        nameField = createNameField();
        saveButton = createStyledButton("保存", Constants.SECONDARY_COLOR);
        undoLastButton = createStyledButton("撤销上一步", Constants.PRIMARY_COLOR);
        undoAllButton = createStyledButton("撤销全部", new Color(155, 89, 182));
        closeButton = createStyledButton("关闭", Constants.ACCENT_COLOR);

        secondRow.add(new JLabel("车位名:"));
        secondRow.add(nameField);
        secondRow.add(saveButton);

        firstRow.add(undoLastButton);
        firstRow.add(undoAllButton);
        firstRow.add(closeButton);
    }

    private JPanel createButtonRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Constants.BUTTON_MARGIN, Constants.BUTTON_MARGIN));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        return panel;
    }

    private JPanel createActionRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, Constants.BUTTON_MARGIN, Constants.BUTTON_MARGIN));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        return panel;
    }

    private JTextField createNameField() {
        JTextField textField = new JTextField(10);
        textField.setFont(Constants.DEFAULT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        textField.setPreferredSize(new Dimension(Constants.TEXT_FIELD_WIDTH, Constants.TEXT_FIELD_HEIGHT));
        return textField;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(Constants.BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(
                Constants.BUTTON_PADDING,
                Constants.BUTTON_PADDING * 2,
                Constants.BUTTON_PADDING,
                Constants.BUTTON_PADDING * 2
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        addButtonHoverEffect(button, bgColor);

        return button;
    }

    private void addButtonHoverEffect(JButton button, Color originalColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(originalColor);
            }
        });
    }

    // Action listener registration methods
    public void addSaveActionListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }

    public void addUndoLastActionListener(ActionListener listener) {
        undoLastButton.addActionListener(listener);
    }

    public void addUndoAllActionListener(ActionListener listener) {
        undoAllButton.addActionListener(listener);
    }

    public void addCloseActionListener(ActionListener listener) {
        closeButton.addActionListener(listener);
    }

    // Getters
    public String getParkingName() {
        return nameField.getText().trim();
    }

    public void clearParkingName() {
        nameField.setText("");
    }
}