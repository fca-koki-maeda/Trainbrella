package com.example.trainbrella.model;

import java.util.List;

public class WeatherResult {
    public String city;
    public String description;
    public double temp;
    public double tempMin;
    public double tempMax;
    public int humidity;
    public double windSpeed;
    public int cloud;
    public int pop;
    public List<HourlyWeather> hourly;
    public String fetchedAt; // 取得時刻

    public WeatherResult(
            String city,
            String description,
            double temp,
            double tempMin,
            double tempMax,
            int humidity,
            double windSpeed,
            int cloud,
            int pop,
                List<HourlyWeather> hourly,
                String fetchedAt
    ) {
        this.city = city;
        this.description = description;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.cloud = cloud;
        this.pop = pop;
        this.hourly = hourly;
        this.fetchedAt = fetchedAt;
    }
}
