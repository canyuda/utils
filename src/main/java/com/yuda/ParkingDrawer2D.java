package com.yuda;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public class ParkingDrawer2D {

    public static void drawRedBoxes(
            String src, String dst, List<ParkingSpot> spots) throws Exception {

        BufferedImage img = ImageIO.read(new File(src));
        Graphics2D g2 = img.createGraphics();
        g2.setStroke(new BasicStroke(4));
        g2.setColor(Color.RED);

        for (ParkingSpot spot : spots) {
            List<ParkingSpot.Point> pts = spot.getCorners();
            if (pts.size() != 4) continue;

            int[] x = pts.stream().mapToInt(p -> (int) p.x).toArray();
            int[] y = pts.stream().mapToInt(p -> (int) p.y).toArray();
            g2.drawPolygon(x, y, 4);
        }
        g2.dispose();
        ImageIO.write(img, "jpg", new File(dst));
        System.out.println("输出文件：" + dst);
    }
}