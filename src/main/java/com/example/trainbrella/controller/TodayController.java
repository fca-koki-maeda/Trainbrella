package com.example.trainbrella.controller;

import com.example.trainbrella.model.Record;
import com.example.trainbrella.model.TrainInfo;
import com.example.trainbrella.model.User;
import com.example.trainbrella.model.WeatherResult;
import com.example.trainbrella.service.RecordService;
import com.example.trainbrella.service.TrainService;
import com.example.trainbrella.service.UserService;
import com.example.trainbrella.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/today")
public class TodayController {

    @Autowired RecordService recordService;
    @Autowired UserService userService;
    @Autowired WeatherService weatherService;
    @Autowired TrainService trainService;

    @GetMapping
    public String today(Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        model.addAttribute("user", users.get(0));

        List<Record> records = recordService.load();
        String today = LocalDate.now().toString();

        model.addAttribute("goDeparted", hasRecord(records, today, "行き", "出発"));
        model.addAttribute("goArrived", hasRecord(records, today, "行き", "到着"));
        model.addAttribute("returnDeparted", hasRecord(records, today, "帰り", "出発"));
        model.addAttribute("returnArrived", hasRecord(records, today, "帰り", "到着"));

        return "today";
    }

    @GetMapping("/form")
    public String form(@RequestParam String type,
                       @RequestParam String action,
                       Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        List<Record> records = recordService.load();
        String today = LocalDate.now().toString();
        Record record = findRecord(records, today, type, action);
        model.addAttribute("user", users.get(0));
        model.addAttribute("type", type);
        model.addAttribute("action", action);
        model.addAttribute("record", record);
        return "today_form";
    }

    @PostMapping
    public String record(
            @RequestParam String type,
            @RequestParam String action,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String delay,
            @RequestParam(required = false) String memo
    ) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        User user = users.get(0);

        String location = resolveLocation(user, type, action);

        WeatherResult w;
        try {
            w = weatherService.getWeather(location);
        } catch (Exception e) {
            w = new WeatherResult("取得失敗", "", 0,0,0,0,0,0,0, java.util.List.of(), "");
        }

        TrainInfo t;
        try {
            t = trainService.getTrainInfo(user.trainLine);
        } catch (Exception e) {
            t = new TrainInfo(user.trainLine, "運行情報を取得できません", null);
        }

        String inferred = (t.status.contains("遅延") || t.status.contains("見合わせ") || t.status.contains("運休")) ? "あり" : "なし";

        Record r = new Record();
        r.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        r.type = type;
        r.action = action;
        r.location = location;
        r.weather = w.description + " " + w.temp + "℃";
        r.trainStatus = t.status;
        r.delayed = (delay == null || delay.isBlank()) ? inferred : delay;
        r.condition = condition;
        r.memo = memo;

        recordService.upsertTodayRecord(r, LocalDate.now().toString());

        return "redirect:/today";
    }

    private String resolveLocation(User user, String type, String action) {
        if ("行き".equals(type)) {
            return "出発".equals(action) ? user.fromCity : user.toCity;
        }
        return "出発".equals(action) ? user.toCity : user.fromCity;
    }

    private boolean hasRecord(List<Record> records, String today, String type, String action) {
        for (Record r : records) {
            boolean isToday = (r.dateTime != null && r.dateTime.startsWith(today))
                    || (r.date != null && r.date.equals(today));
            if (isToday && type.equals(r.type) && action.equals(r.action)) {
                return true;
            }
        }
        return false;
    }

    private Record findRecord(List<Record> records, String today, String type, String action) {
        for (Record r : records) {
            boolean isToday = (r.dateTime != null && r.dateTime.startsWith(today))
                    || (r.date != null && r.date.equals(today));
            if (isToday && type.equals(r.type) && action.equals(r.action)) {
                return r;
            }
        }
        return null;
    }
}