package com.yuda;

import java.awt.*;

public class Constants {
    // 颜色常量
    public static final Color PRIMARY_COLOR = new Color(52, 152, 219);    // 主色调：蓝色
    public static final Color SECONDARY_COLOR = new Color(46, 204, 113);  // 辅助色：绿色
    public static final Color ACCENT_COLOR = new Color(231, 76, 60);      // 强调色：红色
    public static final Color TEXT_COLOR = new Color(44, 62, 80);         // 文本色：深灰
    public static final Color BACKGROUND_COLOR = new Color(245, 247, 250); // 浅灰背景
    public static final Color WHITE_SEMI_TRANSPARENT = new Color(255, 255, 255, 200); // 半透明白色

    // 字体常量
    public static final Font DEFAULT_FONT = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font BUTTON_FONT = new Font("Microsoft YaHei", Font.BOLD, 14);
    public static final Font LABEL_FONT = new Font("Microsoft YaHei", Font.BOLD, 14);

    // 文件扩展名
    public static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    public static final String[] EXCEL_EXTENSIONS = {"xls", "xlsx"};

    // 尺寸常量
    public static final int POINT_SIZE = 10;
    public static final int STROKE_WIDTH = 2;
    public static final int WINDOW_OPACITY = 95; // 百分比
    public static final int TEXT_BACKGROUND_PADDING = 4;
    public static final int TEXT_BACKGROUND_ROUND = 8;
    public static final int CONTROL_PANEL_PADDING = 10;
    public static final int BUTTON_PADDING = 8;
    public static final int BUTTON_MARGIN = 10;
    public static final int TEXT_FIELD_WIDTH = 120;
    public static final int TEXT_FIELD_HEIGHT = 32;

    // 消息常量
    public static final String NO_IMAGE_FILE_MSG = "未选择图片文件，程序将退出。";
    public static final String NO_EXCEL_FILE_MSG = "未选择Excel文件，程序将退出。";
    public static final String ENTER_PARKING_NAME_MSG = "请输入车位名";
    public static final String MIN_POINTS_MSG = "至少需要3个点";
    public static final String MAX_POINTS_MSG = "一次标注最多只能添加4个点";
    public static final String JSON_PARSE_ERROR_MSG = "解析JSON文件失败";
    public static final String FILE_SELECT_ERROR_MSG = "请选择有效的JSON文件";
    // 标题常量
    public static final String APP_TITLE = "车位标注器";
    public static final String SELECT_IMAGE_TITLE = "请选择图片文件";
    public static final String SELECT_EXCEL_TITLE = "请选择Excel文件";
    public static final String IMAGE_FILE_DESCRIPTION = "图片文件";
    public static final String EXCEL_FILE_DESCRIPTION = "Excel文件";

    // Excel相关常量
    public static final String EXCEL_HEADER_NAME = "车位名";
    public static final String EXCEL_HEADER_POINT_PREFIX = "点";
    public static final int MAX_EXCEL_POINTS = 4;

}