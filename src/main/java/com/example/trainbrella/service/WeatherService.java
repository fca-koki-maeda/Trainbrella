package com.example.trainbrella.service;

import com.example.trainbrella.model.WeatherResult;
import com.example.trainbrella.model.HourlyWeather;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class WeatherService {

    @Value("${openweathermap.apiKey}")
    private String apiKey;

    public WeatherResult getWeather(String city) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        // ① Geo APIで日本の都市を解決
        GeoPoint geo = resolveGeo(city, mapper);
        if (geo == null) {
            throw new RuntimeException("都市が見つかりません: " + city);
        }

        // ② lat/lon指定で天気取得（ズレ防止）
        String currentUrl =
                "https://api.openweathermap.org/data/2.5/weather?lat=" +
                        geo.lat +
                        "&lon=" + geo.lon +
                        "&appid=" + apiKey +
                        "&units=metric&lang=ja";

        String forecastUrl =
                "https://api.openweathermap.org/data/2.5/forecast?lat=" +
                        geo.lat +
                        "&lon=" + geo.lon +
                        "&appid=" + apiKey +
                        "&units=metric&lang=ja";

        JsonNode json = mapper.readTree(fetchJson(currentUrl));

        JsonNode weatherNode = json.path("weather");
        if (!weatherNode.isArray() || weatherNode.isEmpty()) {
            throw new RuntimeException("weather情報が取得できません");
        }

        String description = weatherNode.get(0).path("description").asText();
        String actualName = geo.name; // 入力に最も近い日本語名

        double temp = json.path("main").path("temp").asDouble();
        double tempMin = json.path("main").path("temp_min").asDouble();
        double tempMax = json.path("main").path("temp_max").asDouble();
        int humidity = json.path("main").path("humidity").asInt();
        double windSpeed = json.path("wind").path("speed").asDouble();
        int cloud = json.path("clouds").path("all").asInt();

        int pop = 0;
        List<HourlyWeather> hourly = new ArrayList<>();

        try {
            JsonNode forecast = mapper.readTree(fetchJson(forecastUrl));
            JsonNode list = forecast.path("list");

            String today = LocalDate.now().toString();
            String tomorrow = LocalDate.now().plusDays(1).toString();

            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;

            for (JsonNode item : list) {
                String dt = item.path("dt_txt").asText();
                if (dt.startsWith(today) || dt.startsWith(tomorrow)) {
                    double t = item.path("main").path("temp").asDouble();
                    int hourlyHumidity = item.path("main").path("humidity").asInt();
                    double feelsLike = item.path("main").path("feels_like").asDouble();
                    int p = (int) Math.round(item.path("pop").asDouble(0) * 100);
                    double w = item.path("wind").path("speed").asDouble();
                    double rainAmount = 0.0;
                    if (item.has("rain")) {
                        rainAmount = item.path("rain").path("3h").asDouble(
                                item.path("rain").path("1h").asDouble(0.0)
                        );
                    }
                        String time = LocalDateTime.parse(dt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            .format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));

                    String desc = item.path("weather").get(0).path("description").asText();

                    hourly.add(new HourlyWeather(
                            time,
                            desc,
                            t,
                            hourlyHumidity,
                            p,
                            rainAmount,
                                w,
                            feelsLike
                    ));

                    min = Math.min(min, item.path("main").path("temp_min").asDouble());
                    max = Math.max(max, item.path("main").path("temp_max").asDouble());
                }
            }

            if (min != Double.MAX_VALUE) {
                tempMin = min;
                tempMax = max;
            }
        } catch (Exception ignore) {
        }

        String fetchedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return new WeatherResult(
                actualName,
                description,
                temp,
                tempMin,
                tempMax,
                humidity,
                windSpeed,
                cloud,
                pop,
                hourly,
                fetchedAt
        );
    }

    // ===== Geo API =====
    private GeoPoint resolveGeo(String city, ObjectMapper mapper) {
        try {
            String encoded = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url =
                    "https://api.openweathermap.org/geo/1.0/direct?q=" +
                            encoded +
                            ",JP&limit=3&appid=" + apiKey;

            JsonNode arr = mapper.readTree(fetchJson(url));
            if (arr.isArray() && arr.size() > 0) {
                JsonNode n = arr.get(0);
                return new GeoPoint(
                        n.path("lat").asDouble(),
                        n.path("lon").asDouble(),
                        n.path("name").asText(city)
                );
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    private String fetchJson(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");

        Scanner sc = new Scanner(conn.getInputStream());
        StringBuilder sb = new StringBuilder();
        while (sc.hasNext()) sb.append(sc.nextLine());
        sc.close();
        return sb.toString();
    }

    private static class GeoPoint {
        final double lat;
        final double lon;
        final String name;

        GeoPoint(double lat, double lon, String name) {
            this.lat = lat;
            this.lon = lon;
            this.name = name;
        }
    }
}
