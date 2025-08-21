package com.yuda.parking;

import java.util.List;

@lombok.Data
public class Data {
    private BuildingInfo buildingInfo;
    private List<HouseWindow> houseWindow;
    private List<WebProjectUsageList> webProjectUsageList;
}
