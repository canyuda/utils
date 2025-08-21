package com.yuda.parking;

import lombok.Data;

@lombok.Data
public class BuildingInfo {
    private double allArea;
    private long allSets;
    private long avgPrice;
    private double bookedArea;
    private long bookedSets;
    private String bookedSetsToday;
    private long buildId;
    private String buildingName;
    private String fileAddr;
    private String fileNO;
    private String fitment;
    private long isMortgaged;
    private long isSuspended;
    private String permitId;
    private long permitType;
    private double ratio;
    private String saleDate;
    private long sfmjmx;
    private double soldArea;
    private long soldSets;
    private String soldSetsToday;
    private long unsoldSets;
}
