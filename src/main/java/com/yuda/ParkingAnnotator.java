package com.yuda;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ParkingAnnotator extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);    // 主色调：蓝色
    private static final Color SECONDARY_COLOR = new Color(46, 204, 113);  // 辅助色：绿色
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);      // 强调色：红色
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // 文本色：深灰
    private static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    private static final Font BUTTON_FONT = new Font("Microsoft YaHei", Font.BOLD, 14);
    private String IMG_PATH = "D:\\和著府-地下室平面图\\和著府-地下室平面图.jpg";
    private String EXCEL = "D:\\和著府-地下室平面图\\和著府-地下室平面图.xlsx";
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final String[] EXCEL_EXTENSIONS = {"xls", "xlsx"};

    private Image bg;
    private final List<List<Point>> allPolygons = new ArrayList<>();   // 所有已保存车位
    private final List<String> allNames = new ArrayList<>();            // 对应名称
    private final List<Point> currentPoints = new ArrayList<>();        // 正在标注的当前车位
    private JTextField nameField = null;

    private Point dragStartPoint = null;
    private Point viewStartPosition = null;

    public ParkingAnnotator() throws IOException {
        super("车位标注器");

        // 设置窗口图标
        try {
            setIconImage(ImageIO.read(getClass().getResource("/can.ico")));
        } catch (Exception e) {
            // 图标加载失败时不影响主程序
        }

        // 选择图片文件
        IMG_PATH = selectFile("请选择图片文件", IMAGE_EXTENSIONS, "图片文件");
        if (IMG_PATH == null || IMG_PATH.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未选择图片文件，程序将退出。");
            System.exit(0);
            return;
        }

        // 选择Excel文件
        EXCEL = selectFile("请选择Excel文件", EXCEL_EXTENSIONS, "Excel文件");
        if (EXCEL == null || EXCEL.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未选择Excel文件，程序将退出。");
            System.exit(0);
            return;
        }

        bg = new ImageIcon(IMG_PATH).getImage();

        loadExcel();   // 启动时读取历史

        // ---------- 画布 ----------
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, this);

                // 画历史车位
                g.setColor(Color.GREEN);
                for (int i = 0; i < allPolygons.size(); i++) {
                    drawPoly(g, allPolygons.get(i), false, allNames.get(i));
                }

                // 画当前正在标注的车位
                if (!currentPoints.isEmpty()) {
                    g.setColor(Color.RED);
                    List<Point> ordered = orderRect(currentPoints);
                    drawPoly(g, ordered, true, "");
                }

            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(bg.getWidth(this), bg.getHeight(this));
            }

            private void drawPoly(Graphics g, List<Point> ps, boolean isCurrent, String name) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2.5f));  // 加粗线条
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);  // 抗锯齿


                /* 1. 画大圆点 */
                g2.setColor(isCurrent ? ACCENT_COLOR : SECONDARY_COLOR);
                for (Point p : ps) {
                    g2.fillOval(p.x - 5, p.y - 5, 10, 10);  // 增大圆点
                    // 添加圆点边框
                    g2.setColor(Color.WHITE);
                    g2.drawOval(p.x - 5, p.y - 5, 10, 10);
                    g2.setColor(isCurrent ? ACCENT_COLOR : SECONDARY_COLOR);
                }

                /* 2. 画边线 */
                for (int i = 1; i < ps.size(); i++) {
                    Point p1 = ps.get(i - 1);
                    Point p2 = ps.get(i);
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
                if (ps.size() >= 3) {
                    Polygon polygon = new Polygon();
                    ps.forEach(pt -> polygon.addPoint(pt.x, pt.y));
                    g2.draw(polygon);
                }

                /* 3. 在图形内部居中显示车位名（已完成的车位才有名字） */
                if (name != null && !name.isEmpty()) {
                    // 计算多边形的质心作为文字中心
                    int cx = 0, cy = 0;
                    for (Point p : ps) {
                        cx += p.x;
                        cy += p.y;
                    }
                    cx /= ps.size();
                    cy /= ps.size();

                    FontMetrics fm = g2.getFontMetrics();
                    int w = fm.stringWidth(name);
                    int h = fm.getHeight();
                    g2.setColor(new Color(255, 255, 255, 200));  // 调整透明度
                    g2.fillRoundRect(cx - w / 2 - 4, cy - h / 2 - 4, w + 8, h + 8, 8, 8);  // 圆角矩形背景
                    g2.fillRect(cx - w / 2 - 2, cy - h / 2 - 2, w + 4, h + 4);

                    g2.setColor(TEXT_COLOR);  // 使用文本色
                    g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));  // 加粗字体
                    g2.drawString(name, cx - w / 2, cy + h / 2 - fm.getDescent());
                }
            }

            /**
             * 对 2~4 个点实时按“左上→右上→右下→左下”顺序排列，
             * 保证连线无交叉。
             */
            private List<Point> orderRect(List<Point> src) {
                int n = src.size();
                if (n < 2) return new ArrayList<>(src);

                List<Point> pts = new ArrayList<>(src);
                // 1. 按 y 升序，y 相同按 x 升序
                pts.sort(Comparator.comparingInt((Point p) -> p.y).thenComparingInt(p -> p.x));

                if (n == 2) return pts;   // 两点无需处理

                // 2. 取最上面两个点再按 x 升序 -> 左上、右上
                Point topLeft = pts.get(0).x < pts.get(1).x ? pts.get(0) : pts.get(1);
                Point topRight = pts.get(0).x > pts.get(1).x ? pts.get(0) : pts.get(1);

                // 3. 取最下面两个点再按 x 升序 -> 左下、右下
                Point botLeft = pts.get(2).x < (n == 4 ? pts.get(3).x : topLeft.x) ? pts.get(2) :
                        (n == 4 ? pts.get(3) : pts.get(2));
                Point botRight = pts.get(2).x > (n == 4 ? pts.get(3).x : topRight.x) ? pts.get(2) :
                        (n == 4 ? pts.get(3) : pts.get(2));

                // 4 点情况
                if (n == 4) {
                    List<Point> res = new ArrayList<>();
                    res.add(topLeft);
                    res.add(topRight);
                    res.add(botRight);
                    res.add(botLeft);
                    return res;
                }
                return pts; // 3 点暂时不处理，不会出现交叉
            }
        };
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 右键按下 - 准备拖动图片
                if (SwingUtilities.isRightMouseButton(e)) {
                    dragStartPoint = e.getPoint();

                    // 记录当前视图位置
                    Component[] components = getContentPane().getComponents();
                    for (Component component : components) {
                        if (component instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane) component;
                            JViewport viewport = scrollPane.getViewport();
                            viewStartPosition = viewport.getViewPosition();
                            break;
                        }
                    }
                }
                // 左键按下 - 添加标注点
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (currentPoints.size() >= 4) {
                        JOptionPane.showMessageDialog(ParkingAnnotator.this, "一次标注最多只能添加4个点");
                        return;
                    }
                    currentPoints.add(e.getPoint());
                    canvas.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // 滚轮点击 - 撤销上一次点标注
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    if (!currentPoints.isEmpty()) {
                        currentPoints.remove(currentPoints.size() - 1);
                        canvas.repaint();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 右键释放 - 结束拖动
                if (SwingUtilities.isRightMouseButton(e)) {
                    dragStartPoint = null;
                    viewStartPosition = null;
                }
            }
        });

        canvas.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 右键拖动 - 拖动图片
                if (dragStartPoint != null && SwingUtilities.isRightMouseButton(e)) {
                    // 获取滚动面板
                    Component[] components = getContentPane().getComponents();
                    for (Component component : components) {
                        if (component instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane) component;
                            JViewport viewport = scrollPane.getViewport();

                            // 计算视图位置的变化量
                            int dx = dragStartPoint.x - e.getX();
                            int dy = dragStartPoint.y - e.getY();

                            // 计算新的视图位置
                            int newX = Math.max(0, Math.min(viewStartPosition.x + dx,
                                    canvas.getWidth() - viewport.getWidth()));
                            int newY = Math.max(0, Math.min(viewStartPosition.y + dy,
                                    canvas.getHeight() - viewport.getHeight()));

                            // 设置新的视图位置
                            viewport.setViewPosition(new Point(newX, newY));
                            break;
                        }
                    }
                }
                // 左键拖动 - 更新最后一个点的位置（如果正在标注）
                else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (currentPoints.size() >= 4) {
                        return;
                    }

                    // 更新最后一个点的位置
                    if (!currentPoints.isEmpty()) {
                        currentPoints.set(currentPoints.size() - 1, e.getPoint());
                        canvas.repaint();
                    }
                }
            }
        });

        canvas.setPreferredSize(new Dimension(bg.getWidth(this), bg.getHeight(this)));
        // 设置窗口背景
        getContentPane().setBackground(Color.WHITE);

        // 添加窗口阴影 (Windows平台)
        try {
            Class<?> awtUtil = Class.forName("com.sun.awt.AWTUtilities");
            Method setWindowOpacity = awtUtil.getMethod("setWindowOpacity", Window.class, float.class);
            setWindowOpacity.invoke(null, this, 0.95f);
        } catch (Exception e) {
            // 非Windows平台或不支持时忽略
        }
        // ---------- 控制面板 ----------
        JPanel ctrl = new JPanel();
        ctrl.setLayout(new BoxLayout(ctrl, BoxLayout.Y_AXIS)); // 使用垂直布局
        ctrl.setBackground(new Color(245, 247, 250));  // 浅灰背景
        ctrl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // 内边距

        JPanel firstRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JPanel secondRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        firstRow.setBackground(Color.WHITE);
        secondRow.setBackground(Color.WHITE);
        firstRow.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        secondRow.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel label = new JLabel("车位名:");
        label.setFont(DEFAULT_FONT);
        label.setForeground(TEXT_COLOR);

        // 美化文本框
        nameField = new JTextField(10);
        nameField.setFont(DEFAULT_FONT);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        nameField.setPreferredSize(new Dimension(120, 32));

        JButton saveBtn = createStyledButton("保存", SECONDARY_COLOR);
        JButton undoBtn = createStyledButton("撤销全部", new Color(155, 89, 182));
        JButton undoPreBtn = createStyledButton("撤销上一步", new Color(52, 152, 219));
        JButton closeBtn = createStyledButton("关闭", ACCENT_COLOR);

        firstRow.add(label);
        firstRow.add(nameField);
        firstRow.add(saveBtn);

        secondRow.add(undoPreBtn);
        secondRow.add(undoBtn);
        secondRow.add(closeBtn);

        ctrl.add(firstRow);
        ctrl.add(secondRow);

        undoPreBtn.addActionListener(e -> {
            if (!currentPoints.isEmpty()) {
                currentPoints.remove(currentPoints.size() - 1);
                canvas.repaint();
            }
        });

        undoBtn.addActionListener(e -> {
            if (!currentPoints.isEmpty()) {
                currentPoints.clear();
                canvas.repaint();
            }
        });

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入车位名");
                return;
            }
            if (currentPoints.size() < 3) {
                JOptionPane.showMessageDialog(this, "至少需要3个点");
                return;
            }
            List<Point> ordered = order4Points(currentPoints);
            appendExcel(name, ordered);
            allPolygons.add(new ArrayList<>(ordered));
            allNames.add(name);
            currentPoints.clear();
            nameField.setText("");
            canvas.repaint();
        });

        closeBtn.addActionListener(e -> System.exit(0));

        add(ctrl, BorderLayout.NORTH);
        add(new JScrollPane(canvas), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // 2. 计算窗口尺寸为屏幕的二分之一
        int windowWidth = screenWidth * 2 / 3;
        int windowHeight = screenHeight * 2 / 3;

        // 3. 设置窗口大小
        setSize(windowWidth, windowHeight);

        // 4. 将窗口定位在屏幕正中央
        setLocationRelativeTo(null);

        // 添加组件监听器，在组件显示后调整滚动条位置
        SwingUtilities.invokeLater(() -> {
            // 获取滚动面板
            Component[] components = getContentPane().getComponents();
            for (Component component : components) {
                if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    JViewport viewport = scrollPane.getViewport();

                    // 如果已有标注数据，计算最后一条标注信息的位置
                    if (!allPolygons.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            // 获取最后一条标注信息的位置
                            List<Point> lastPolygon = allPolygons.get(allPolygons.size() - 1);
                            if (!lastPolygon.isEmpty()) {
                                // 计算多边形的质心作为定位点
                                int cx = 0, cy = 0;
                                for (Point p : lastPolygon) {
                                    cx += p.x;
                                    cy += p.y;
                                }
                                cx /= lastPolygon.size();
                                cy /= lastPolygon.size();

                                // 将该点定位到视图的左上角附近
                                Point target = new Point(Math.max(0, cx - 100), Math.max(0, cy - 100));
                                viewport.setViewPosition(target);
                            }
                        });
                    }
                    break;
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);  // 移除焦点边框
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));  // 手型光标

        // 添加悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }


    // 添加文件选择方法
    private String selectFile(String dialogTitle, String[] extensions, String description) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);

        // 设置文件过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                description + " (" + String.join(", ", extensions) + ")",
                extensions
        );
        fileChooser.setFileFilter(filter);

        // 设置默认目录为程序所在文件夹
        // 获取当前类所在的jar文件或class文件的路径
        try {
            File currentFile = new File(ParkingAnnotator.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            // 如果是jar文件，获取其所在的目录；如果是class文件，获取其父目录
            File defaultDir = currentFile.isDirectory() ? currentFile : currentFile.getParentFile();
            fileChooser.setCurrentDirectory(defaultDir);
        } catch (Exception e) {
            // 如果获取失败，则使用用户主目录作为备选
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return selectedFile.getAbsolutePath();
        }

        return null;
    }

    /**
     * 对 4 个点按顺时针排序，返回新 List（原列表不变）
     */
    private List<Point> order4Points(List<Point> src) {
        if (src.size() != 4) return new ArrayList<>(src); // 非4点直接返回

        // 1. 计算质心
        int cx = 0, cy = 0;
        for (Point p : src) {
            cx += p.x;
            cy += p.y;
        }
        cx /= 4;
        cy /= 4;

        // 2. 按与质心夹角排序（顺时针）
        List<Point> list = new ArrayList<>(src);
        final int fcx = cx, fcy = cy;
        list.sort((a, b) -> {
            double da = Math.atan2(a.y - fcy, a.x - fcx);
            double db = Math.atan2(b.y - fcy, b.x - fcx);
            return Double.compare(db, da);   // 降序 -> 顺时针
        });
        return list;
    }

    // 修改 loadExcel 方法
    private void loadExcel() {
        File f = new File(EXCEL);
        if (!f.exists()) return;
        try (FileInputStream in = new FileInputStream(f);
             Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);

            // 用于跟踪是否有任何行需要更新
            boolean hasChanges = false;
            List<RowData> rowDataList = new ArrayList<>();

            // 先读取所有数据
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    // 保存表头
                    rowDataList.add(new RowData(row.getRowNum(), null, null));
                    continue; // skip header
                }

                String name = row.getCell(0).getStringCellValue();
                List<Point> pts = new ArrayList<>();
                for (int i = 1; i < row.getLastCellNum(); i++) {
                    String val = row.getCell(i).getStringCellValue();
                    if (val == null || val.trim().isEmpty()) continue;
                    String[] xy = val.replaceAll("[()]", "").split(",");
                    pts.add(new Point(Integer.parseInt(xy[0].trim()),
                            Integer.parseInt(xy[1].trim())));
                }

                // 保存原始数据
                RowData rowData = new RowData(row.getRowNum(), name, new ArrayList<>(pts));
                rowDataList.add(rowData);

                // 检查点的顺序并调整为左上、右上、右下、左下
                if (pts.size() == 4) {
                    List<Point> reordered = reorderPoints(pts);
                    // 检查是否需要重新排序
                    if (!pts.equals(reordered)) {
                        // 更新数据
                        allNames.add(name);
                        allPolygons.add(reordered);
                        // 标记有改动
                        hasChanges = true;
                        // 更新行数据
                        rowData.points = reordered;
                    } else {
                        allNames.add(name);
                        allPolygons.add(pts);
                    }
                } else {
                    allNames.add(name);
                    allPolygons.add(pts);
                }
            }

            // 如果有改动，将修改后的数据写回Excel
            if (hasChanges) {
                try (FileOutputStream out = new FileOutputStream(f)) {
                    Workbook writeWb = new XSSFWorkbook();
                    Sheet writeSheet = writeWb.createSheet();

                    // 写入表头
                    Row headerRow = writeSheet.createRow(0);
                    headerRow.createCell(0).setCellValue("车位名");
                    for (int i = 0; i < 20; i++) {
                        headerRow.createCell(i + 1).setCellValue("点" + (i + 1));
                    }

                    // 写入数据行
                    for (RowData rowData : rowDataList) {
                        if (rowData.rowNum == 0) continue; // 跳过表头

                        Row dataRow = writeSheet.createRow(rowData.rowNum);
                        dataRow.createCell(0).setCellValue(rowData.name);

                        if (rowData.points != null) {
                            int idx = 1;
                            for (Point pt : rowData.points) {
                                dataRow.createCell(idx++).setCellValue("(" + pt.x + "," + pt.y + ")");
                            }
                        }
                    }

                    writeWb.write(out);
                    writeWb.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 添加辅助类用于保存行数据
    private static class RowData {
        int rowNum;
        String name;
        List<Point> points;

        RowData(int rowNum, String name, List<Point> points) {
            this.rowNum = rowNum;
            this.name = name;
            this.points = points;
        }
    }

    private List<Point> reorderPoints(List<Point> points) {
        if (points.size() != 4) return points;

        // 1. 按 y 值排序，找到上方两个点和下方两个点
        List<Point> sortedByY = new ArrayList<>(points);
        sortedByY.sort(Comparator.comparingInt(p -> p.y));

        // 2. 上方两个点按 x 值排序，确定左上和右上
        List<Point> topPoints = new ArrayList<>();
        topPoints.add(sortedByY.get(0));
        topPoints.add(sortedByY.get(1));
        topPoints.sort(Comparator.comparingInt(p -> p.x));
        Point topLeft = topPoints.get(0);
        Point topRight = topPoints.get(1);

        // 3. 下方两个点按 x 值排序，确定右下和左下
        List<Point> bottomPoints = new ArrayList<>();
        bottomPoints.add(sortedByY.get(2));
        bottomPoints.add(sortedByY.get(3));
        bottomPoints.sort(Comparator.comparingInt(p -> p.x));
        Point bottomRight = bottomPoints.get(1);
        Point bottomLeft = bottomPoints.get(0);

        // 4. 按照左上、右上、右下、左下顺序返回
        List<Point> reordered = new ArrayList<>();
        reordered.add(topLeft);
        reordered.add(topRight);
        reordered.add(bottomRight);
        reordered.add(bottomLeft);

        return reordered;
    }

    // ---------- Excel 追加 ----------
    private void appendExcel(String name, List<Point> pts) {
        File f = new File(EXCEL);
        Workbook wb;
        Sheet sheet;
        try {
            if (!f.exists()) {            // 新建文件
                wb = new XSSFWorkbook();
                sheet = wb.createSheet();
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("车位名");
                for (int i = 0; i < 20; i++) header.createCell(i + 1).setCellValue("点" + (i + 1));
            } else {                      // 追加
                try (FileInputStream in = new FileInputStream(f)) {
                    wb = WorkbookFactory.create(in);
                }
                sheet = wb.getSheetAt(0);
            }
            int last = sheet.getLastRowNum();
            Row row = sheet.createRow(last + 1);
            row.createCell(0).setCellValue(name);
            int idx = 1;
            for (Point pt : pts) {
                row.createCell(idx++).setCellValue("(" + pt.x + "," + pt.y + ")");
            }
            try (FileOutputStream out = new FileOutputStream(f)) {
                wb.write(out);
            }
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
