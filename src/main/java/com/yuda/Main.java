package com.yuda;

import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ParkingAnnotator().setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
