package com.example.trainbrella.model;

public class TrainInfo {
    public String lineName;
    public String status;
    public String url;

    public TrainInfo(String lineName, String status, String url) {
        this.lineName = lineName;
        this.status = status;
        this.url = url;
    }
}