package com.example.trainbrella.model;

public class HourlyWeather {
    public String time;      // "HH:mm"
    public String description;
    public double temp;
    public int humidity;
    public int pop;
    public double rainAmount;
    public double windSpeed;
    public double feelsLike;

    public HourlyWeather(
            String time,
            String description,
            double temp,
            int humidity,
            int pop,
            double rainAmount,
                double windSpeed,
            double feelsLike
    ) {
        this.time = time;
        this.description = description;
        this.temp = temp;
        this.humidity = humidity;
        this.pop = pop;
        this.rainAmount = rainAmount;
        this.windSpeed = windSpeed;
        this.feelsLike = feelsLike;
    }
}