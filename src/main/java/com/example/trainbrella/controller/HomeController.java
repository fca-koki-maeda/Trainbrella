package com.example.trainbrella.controller;

import com.example.trainbrella.model.User;
import com.example.trainbrella.model.WeatherResult;
import com.example.trainbrella.model.TrainInfo;
import com.example.trainbrella.service.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    WeatherService weatherService;

    @Autowired
    TrainService trainService;

    @Autowired
    RiskService riskService;

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @GetMapping("/")
    public String index(Model model) throws Exception {
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
            fromWeather = emptyWeather("出発地");
        }
        try {
            toWeather = weatherService.getWeather(user.toCity);
        } catch (Exception e) {
            toWeather = emptyWeather("到着地");
        }

        List<String> lines = user.getTrainLinesDisplay();
        if (lines == null || lines.isEmpty()) {
            lines = new ArrayList<>();
            if (user.trainLine != null && !user.trainLine.isBlank()) lines.add(user.trainLine);
        }

        List<TrainInfo> trainInfos = new ArrayList<>();
        for (String line : lines) {
            try {
                trainInfos.add(trainService.getTrainInfo(line));
            } catch (Exception e) {
                trainInfos.add(new TrainInfo(line, "運行情報を取得できません", null));
            }
        }

        String trainStatus = "平常運転";
        for (TrainInfo t : trainInfos) {
            if (t.status != null && (t.status.contains("遅延") || t.status.contains("見合わせ") || t.status.contains("運休"))) {
                trainStatus = "遅延";
                break;
            }
        }

        int riskFrom = riskService.calculate(fromWeather, trainStatus);
        int riskTo = riskService.calculate(toWeather, trainStatus);
        int risk = Math.max(riskFrom, riskTo);

        model.addAttribute("fromWeather", fromWeather);
        model.addAttribute("toWeather", toWeather);
        model.addAttribute("trainInfos", trainInfos);
        model.addAttribute("risk", risk);
        model.addAttribute("user", user);

        boolean needUmbrella =
                maxHourlyPop(fromWeather) >= 30
                || maxHourlyPop(toWeather) >= 30
                || hasRain(fromWeather)
                || hasRain(toWeather);

        model.addAttribute(
                "umbrella",
                needUmbrella ? "傘を持っていきましょう" : "傘は不要です"
        );

        // 既存の通知メール送信処理がある場合は、本文だけ差し替え
        if (user.email != null && !user.email.isBlank()) {
            String umbrellaLine = needUmbrella ? "本日は傘を持っていきましょう。" : "本日は傘は不要です。";
            String greeting = getGreetingNoBang();
            String body = buildStartupMailBody(umbrellaLine, trainInfos, risk);
            mailService.send(user.email, "【Trainbrella】" + greeting, body);
        }

        return "index";
    }

    private String buildStartupMailBody(String umbrellaLine, List<TrainInfo> trainInfos, int risk) {
        boolean allNormal = true;
        for (TrainInfo t : trainInfos) {
            if (t.status == null || !t.status.contains("平常運転")) {
                allNormal = false;
                break;
            }
        }
        String lineStatus = allNormal ? "路線はすべて平常運転です。" : "一部遅れ・運休の路線があります。";

        return umbrellaLine + "\n"
                + lineStatus + "\n"
                + "遅延リスクスコアは" + risk + "です。" + "\n"
                + "出発・到着の際はぜひ記録しましょう。";
    }

    private String getGreetingNoBang() {
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(4, 0)) && now.isBefore(LocalTime.of(11, 0))) {
            return "おはようございます";
        } else if (!now.isBefore(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(17, 0))) {
            return "こんにちは";
        }
        return "こんばんは";
    }

    private WeatherResult emptyWeather(String label) {
        return new WeatherResult(
                label + " 取得失敗",
                "",
                0, 0, 0,
                0, 0, 0, 0,
                java.util.List.of(),
                ""
        );
    }

    private int maxHourlyPop(WeatherResult w) {
        int max = w.pop;
        if (w.hourly != null) {
            for (var h : w.hourly) {
                if (h.pop > max) max = h.pop;
            }
        }
        return max;
    }

    private boolean hasRain(WeatherResult w) {
        if (w.description != null && w.description.contains("雨")) {
            return true;
        }
        if (w.hourly != null) {
            for (var h : w.hourly) {
                if (h.description != null && h.description.contains("雨")) {
                    return true;
                }
            }
        }
        return false;
    }
}
