package com.yuda;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExcelHandler {
    private final String filePath;

    public ExcelHandler(String filePath) {
        this.filePath = filePath;
    }

    public List<ParkingSpotData> loadParkingSpots() {
        List<ParkingSpotData> parkingSpots = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return parkingSpots;
        }

        try (FileInputStream in = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<RowData> rowDataList = new ArrayList<>();
            boolean hasChanges = false;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    rowDataList.add(new RowData(row.getRowNum(), null, null));
                    continue;
                }

                String name = row.getCell(0).getStringCellValue();
                List<Point> points = new ArrayList<>();

                for (int i = 1; i < row.getLastCellNum(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) continue;

                    String val = cell.getStringCellValue();
                    if (val == null || val.trim().isEmpty()) continue;

                    String[] xy = val.replaceAll("[()]", "").split(",");
                    points.add(new Point(
                            Integer.parseInt(xy[0].trim()),
                            Integer.parseInt(xy[1].trim())
                    ));
                }

                RowData rowData = new RowData(row.getRowNum(), name, new ArrayList<>(points));
                rowDataList.add(rowData);

                if (points.size() == 4) {
                    List<Point> reordered = reorderPoints(points);
                    if (!points.equals(reordered)) {
                        parkingSpots.add(new ParkingSpotData(name, reordered));
                        hasChanges = true;
                        rowData.points = reordered;
                    } else {
                        parkingSpots.add(new ParkingSpotData(name, points));
                    }
                } else {
                    parkingSpots.add(new ParkingSpotData(name, points));
                }
            }

            if (hasChanges) {
                updateExcelFile(rowDataList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return parkingSpots;
    }

    public void appendParkingSpot(String name, List<Point> points) {
        File file = new File(filePath);
        Workbook workbook;
        Sheet sheet;

        try {
            if (!file.exists()) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet();
                createHeaderRow(sheet);
            } else {
                try (FileInputStream in = new FileInputStream(file)) {
                    workbook = WorkbookFactory.create(in);
                }
                sheet = workbook.getSheetAt(0);
            }

            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.createRow(lastRowNum + 1);

            row.createCell(0).setCellValue(name);

            int cellIndex = 1;
            for (Point point : points) {
                row.createCell(cellIndex++).setCellValue(
                        String.format("(%d,%d)", point.x, point.y)
                );
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                workbook.write(out);
            }
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(Constants.EXCEL_HEADER_NAME);

        for (int i = 0; i < Constants.MAX_EXCEL_POINTS; i++) {
            header.createCell(i + 1).setCellValue(
                    Constants.EXCEL_HEADER_POINT_PREFIX + (i + 1)
            );
        }
    }

    private void updateExcelFile(List<RowData> rowDataList) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet();
            createHeaderRow(sheet);

            for (RowData rowData : rowDataList) {
                if (rowData.rowNum == 0) continue;

                Row row = sheet.createRow(rowData.rowNum);
                row.createCell(0).setCellValue(rowData.name);

                if (rowData.points != null) {
                    int cellIndex = 1;
                    for (Point point : rowData.points) {
                        row.createCell(cellIndex++).setCellValue(
                                String.format("(%d,%d)", point.x, point.y)
                        );
                    }
                }
            }

            workbook.write(out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Point> reorderPoints(List<Point> points) {
        if (points.size() != 4) return points;

        List<Point> sortedByY = new ArrayList<>(points);
        sortedByY.sort(Comparator.comparingInt(p -> (int) p.getY()));

        List<Point> topPoints = new ArrayList<>();
        topPoints.add(sortedByY.get(0));
        topPoints.add(sortedByY.get(1));
        topPoints.sort(Comparator.comparingInt(p -> (int) p.getX()));

        List<Point> bottomPoints = new ArrayList<>();
        bottomPoints.add(sortedByY.get(2));
        bottomPoints.add(sortedByY.get(3));
        bottomPoints.sort(Comparator.comparingInt(p -> (int) p.getX()));

        List<Point> reordered = new ArrayList<>();
        reordered.add(topPoints.get(0));  // 左上
        reordered.add(topPoints.get(1));  // 右上
        reordered.add(bottomPoints.get(1)); // 右下
        reordered.add(bottomPoints.get(0)); // 左下

        return reordered;
    }

    public static class ParkingSpotData {
        private final String name;
        private final List<Point> points;

        public ParkingSpotData(String name, List<Point> points) {
            this.name = name;
            this.points = points;
        }

        public String getName() {
            return name;
        }

        public List<Point> getPoints() {
            return points;
        }
    }

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
}