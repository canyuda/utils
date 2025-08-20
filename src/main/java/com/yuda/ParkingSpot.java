package com.yuda;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ParkingSpot {
    private String id;                 // 车位号，如 A001
    private List<Point> corners;       // 4 个角

    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    // getter / setter 省略
}