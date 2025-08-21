package com.yuda;

import com.alibaba.fastjson2.JSON;
import com.yuda.parking.Data;
import com.yuda.parking.HousewindowList;
import com.yuda.parking.ParkingDTO;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkingAnnotator extends JFrame {
    private String imagePath;
    private String excelPath;
    private final CanvasPanel canvasPanel;
    private final ControlPanel controlPanel;
    private final ExcelHandler excelHandler;

    public ParkingAnnotator() throws IOException {
        super(Constants.APP_TITLE);

        setupWindowIcon();
        setupFilePaths();

        Image backgroundImage = new ImageIcon(imagePath).getImage();
        this.canvasPanel = new CanvasPanel(backgroundImage);
        this.excelHandler = new ExcelHandler(excelPath);
        this.controlPanel = new ControlPanel();

        setupComponents();
        setupEventListeners();
        setupWindowProperties();

        loadExistingParkingSpots();
    }

    private void setupWindowIcon() {
        try {
            setIconImage(ImageIO.read(getClass().getResource("/can.ico")));
        } catch (Exception e) {
            // 图标加载失败不影响主程序
        }
    }

    private void setupFilePaths() {
        File currentDir = new File(System.getProperty("user.dir"));

        List<File> imageFiles = findFilesByExtensions(currentDir, Constants.IMAGE_EXTENSIONS);
        List<File> excelFiles = findFilesByExtensions(currentDir, Constants.EXCEL_EXTENSIONS);

        if (imageFiles.size() == 1 && excelFiles.size() == 1) {
            imagePath = imageFiles.get(0).getAbsolutePath();
            excelPath = excelFiles.get(0).getAbsolutePath();
        } else {
            imagePath = selectFile(Constants.SELECT_IMAGE_TITLE,
                    Constants.IMAGE_EXTENSIONS,
                    Constants.IMAGE_FILE_DESCRIPTION);

            if (imagePath == null || imagePath.isEmpty()) {
                JOptionPane.showMessageDialog(this, Constants.NO_IMAGE_FILE_MSG);
                System.exit(0);
            }

            excelPath = selectFile(Constants.SELECT_EXCEL_TITLE,
                    Constants.EXCEL_EXTENSIONS,
                    Constants.EXCEL_FILE_DESCRIPTION);

            if (excelPath == null || excelPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, Constants.NO_EXCEL_FILE_MSG);
                System.exit(0);
            }
        }
    }

    private void setupWindowProperties() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupWindowSize();
        setLocationRelativeTo(null);
        setupWindowShadow();
    }

    private void setupWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int windowWidth = screenSize.width * 2 / 3;
        int windowHeight = screenSize.height * 2 / 3;
        setSize(windowWidth, windowHeight);
    }

    private void setupWindowShadow() {
        try {
            Class<?> awtUtil = Class.forName("com.sun.awt.AWTUtilities");
            Method setWindowOpacity = awtUtil.getMethod("setWindowOpacity", Window.class, float.class);
            setWindowOpacity.invoke(null, this, Constants.WINDOW_OPACITY / 100.0f);
        } catch (Exception e) {
            // 非Windows平台或不支持时忽略
        }
    }

    private void loadExistingParkingSpots() {
        List<ExcelHandler.ParkingSpotData> parkingSpots = excelHandler.loadParkingSpots();

        for (ExcelHandler.ParkingSpotData spot : parkingSpots) {
            canvasPanel.addSavedPolygon(spot.getPoints(), spot.getName());
        }

        scrollToLastParkingSpot();
    }

    private void scrollToLastParkingSpot() {
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) getContentPane().getComponent(1);
            JViewport viewport = scrollPane.getViewport();

            List<List<Point>> polygons = canvasPanel.getSavedPolygons();
            if (!polygons.isEmpty()) {
                List<Point> lastPolygon = polygons.get(polygons.size() - 1);
                if (!lastPolygon.isEmpty()) {
                    int centerX = calculateCenterX(lastPolygon);
                    int centerY = calculateCenterY(lastPolygon);

                    Point target = new Point(
                            Math.max(0, centerX - 100),
                            Math.max(0, centerY - 100)
                    );

                    viewport.setViewPosition(target);
                }
            }
        });
    }

    private int calculateCenterX(List<Point> points) {
        return points.stream().mapToInt(p -> (int) p.getX()).sum() / points.size();
    }

    private int calculateCenterY(List<Point> points) {
        return points.stream().mapToInt(p -> (int) p.getY()).sum() / points.size();
    }

    private List<File> findFilesByExtensions(File directory, String[] extensions) {
        List<File> result = new ArrayList<>();
        if (directory == null || !directory.isDirectory()) {
            return result;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return result;
        }

        for (File file : files) {
            if (file.isFile() && hasExtension(file, extensions)) {
                result.add(file);
            }
        }

        return result;
    }

    private boolean hasExtension(File file, String[] extensions) {
        String fileName = file.getName().toLowerCase();
        for (String ext : extensions) {
            if (fileName.endsWith("." + ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String selectFile(String dialogTitle, String[] extensions, String description) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);

        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                description + " (" + String.join(", ", extensions) + ")",
                extensions
        );
        fileChooser.setFileFilter(filter);

        setupFileChooserDirectory(fileChooser);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }

        return null;
    }

    private void setupFileChooserDirectory(JFileChooser fileChooser) {
        try {
            File currentFile = new File(ParkingAnnotator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());

            File defaultDir = currentFile.isDirectory() ?
                    currentFile : currentFile.getParentFile();

            fileChooser.setCurrentDirectory(defaultDir);
        } catch (Exception e) {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
    }

    private void setupComponents() {
        add(controlPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(canvasPanel);
        add(scrollPane, BorderLayout.CENTER);

        new CanvasMouseHandler(canvasPanel, scrollPane);
    }

    private void setupEventListeners() {
        controlPanel.addSaveActionListener(new SaveAction());
        controlPanel.addUndoLastActionListener(new UndoLastAction());
        controlPanel.addUndoAllActionListener(new UndoAllAction());
        controlPanel.addCloseActionListener(e -> System.exit(0));
        controlPanel.setupSelectFileButtonListener(new SelectFileAction());
    }

    private class SaveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String parkingName = controlPanel.getParkingName();
            List<Point> currentPoints = canvasPanel.getCurrentPoints();

            if (parkingName.isEmpty()) {
                JOptionPane.showMessageDialog(ParkingAnnotator.this, Constants.ENTER_PARKING_NAME_MSG);
                return;
            }

            if (currentPoints.size() < 3) {
                JOptionPane.showMessageDialog(ParkingAnnotator.this, Constants.MIN_POINTS_MSG);
                return;
            }

            List<Point> orderedPoints = canvasPanel.orderPointsForSaving(currentPoints);
            excelHandler.appendParkingSpot(parkingName, orderedPoints);
            canvasPanel.addSavedPolygon(orderedPoints, parkingName);

            canvasPanel.clearCurrentPoints();
            controlPanel.clearParkingName();
        }
    }

    private class UndoLastAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            canvasPanel.removeLastCurrentPoint();
        }
    }

    private class UndoAllAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            canvasPanel.clearCurrentPoints();
        }
    }

    private class SelectFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择JSON文件");

            // 设置文件过滤器，只显示.json文件
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".json");
                }

                @Override
                public String getDescription() {
                    return "JSON文件 (*.json)";
                }
            });

            int returnValue = fileChooser.showOpenDialog(controlPanel);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // 读取文件内容
                    String jsonContent = readFileToString(selectedFile);

                    // 解析为ParkingDTO对象
                    ParkingDTO parkingDTO = JSON.parseObject(jsonContent, ParkingDTO.class);

                    // 获取所需数据
                    Data data = parkingDTO.getData();

                    List<HousewindowList> houseWindowList = data.getHouseWindow().get(0).getHousewindowList();

                    // 创建车位名到状态的映射
                    Map<String, String> roomStatusMap = new HashMap<>();
                    for (HousewindowList item : houseWindowList) {
                        String room = item.getRoom();
                        int presaleid = item.getPresaleid();
                        String status = getStatusByPresaleId(presaleid);
                        if (status != null) {
                            roomStatusMap.put(room, status);
                        }
                    }
                    try {
                        excelHandler.updateParkingStatus(roomStatusMap);
                        JOptionPane.showMessageDialog(controlPanel, "Excel文件更新成功!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(controlPanel, "更新Excel文件失败: " + ex.getMessage());
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(controlPanel, "文件读取失败: " + ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(controlPanel, "JSON解析失败: " + ex.getMessage());
                }
            }
        }

        private String getStatusByPresaleId(int presaleid) {
            switch (presaleid) {
                case 0:
                    return "未售";
                case 2:
                    return "已签约";
                case 3:
                    return "已认购";
                case 8:
                    return "已备案";
                default:
                    return null;
            }
        }
    }

    private static String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        // 移除最后一个换行符
        if (content.length() > 0) {
            content.setLength(content.length() - System.lineSeparator().length());
        }
        return content.toString();
    }


}