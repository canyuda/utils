package com.yuda.parking;

@lombok.Data
public class ParkingDTO {
    private String code;
    private Data data;
    private String ext;
    private String message;
    private String messageArgs;
    private boolean print;
    private String resolve;
}
