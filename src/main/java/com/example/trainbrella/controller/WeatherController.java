package com.example.trainbrella.controller;

import com.example.trainbrella.model.User;
import com.example.trainbrella.model.WeatherResult;
import com.example.trainbrella.service.UserService;
import com.example.trainbrella.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WeatherController {

    @Autowired WeatherService weatherService;
    @Autowired UserService userService;

    @GetMapping("/weather")
    public String weather(Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        User user = users.get(0);

        WeatherResult fromWeather;
        WeatherResult toWeather;
        try {
            fromWeather = weatherService.getWeather(user.fromCity);
        } catch (Exception e) {
            fromWeather = new WeatherResult("出発地 取得失敗", "", 0,0,0,0,0,0,0, java.util.List.of(), "");
        }
        try {
            toWeather = weatherService.getWeather(user.toCity);
        } catch (Exception e) {
            toWeather = new WeatherResult("到着地 取得失敗", "", 0,0,0,0,0,0,0, java.util.List.of(), "");
        }

        model.addAttribute("fromWeather", fromWeather);
        model.addAttribute("toWeather", toWeather);
        model.addAttribute("user", user);

        return "weather";
    }
}