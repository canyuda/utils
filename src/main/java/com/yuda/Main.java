package com.yuda;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        List<ParkingSpot> list = Arrays.asList(
                new ParkingSpot("A001", Arrays.asList(
                        new ParkingSpot.Point(659, 854),
                        new ParkingSpot.Point(720, 854),
                        new ParkingSpot.Point(720, 962),
                        new ParkingSpot.Point(659, 962)
                )),
                new ParkingSpot("A002", Arrays.asList(
                        new ParkingSpot.Point(110, 310),
                        new ParkingSpot.Point(190, 310),
                        new ParkingSpot.Point(190, 370),
                        new ParkingSpot.Point(110, 370)
                ))
        );
        ParkingDrawer2D.drawRedBoxes("D:\\和著府-地下室平面图.jpg", "D:\\和著府-地下室平面图_标注.jpg", list);
    }
}