package com.example.trainbrella.controller;

import com.example.trainbrella.model.TrainInfo;
import com.example.trainbrella.model.User;
import com.example.trainbrella.service.TrainService;
import com.example.trainbrella.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TrainController {

    @Autowired TrainService trainService;
    @Autowired UserService userService;

    @GetMapping("/train")
    public String train(Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        User user = users.get(0);

        List<String> lines = user.getTrainLinesDisplay();
        if (lines == null || lines.isEmpty()) {
            lines = new ArrayList<>();
            if (user.trainLine != null && !user.trainLine.isBlank()) lines.add(user.trainLine);
        }

        List<TrainInfo> infos = new ArrayList<>();
        for (String line : lines) {
            TrainInfo info;
            try {
                info = trainService.getTrainInfo(line);
            } catch (Exception e) {
                info = new TrainInfo(line, "運行情報を取得できません", null);
            }

            String fallback =
                    "https://transit.yahoo.co.jp/diainfo/search?q=" +
                    URLEncoder.encode(line, StandardCharsets.UTF_8);
            if (info.url == null) info.url = fallback;

            infos.add(info);
        }

        model.addAttribute("trainInfos", infos);
        model.addAttribute("user", user);

        return "train";
    }
}