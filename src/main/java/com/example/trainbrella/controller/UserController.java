package com.example.trainbrella.controller;

import com.example.trainbrella.model.User;
import com.example.trainbrella.service.UserService;
import com.example.trainbrella.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    MailService mailService;

    @GetMapping
    public String form(Model model) {
        List<User> users = userService.load();
        User user = (users == null || users.isEmpty()) ? new User() : users.get(0);

        if (user.trainLines == null) user.trainLines = new ArrayList<>();
        while (user.trainLines.size() < 5) user.trainLines.add("");
        if (user.trainLine != null && !user.trainLine.isBlank()
                && (user.trainLines.get(0) == null || user.trainLines.get(0).isBlank())) {
            user.trainLines.set(0, user.trainLine);
        }

        model.addAttribute("user", user);
        return "user";
    }

    @PostMapping
    public String submit(User user) {
        ArrayList<String> lines = new ArrayList<>();
        if (user.trainLines != null) {
            for (String s : user.trainLines) {
                if (s != null && !s.trim().isEmpty()) lines.add(s.trim());
            }
        }
        if (lines.isEmpty() && user.trainLine != null && !user.trainLine.trim().isEmpty()) {
            lines.add(user.trainLine.trim());
        }
        if (lines.size() > 5) lines = new ArrayList<>(lines.subList(0, 5));
        user.trainLines = lines;
        user.trainLine = lines.isEmpty() ? user.trainLine : lines.get(0);

        userService.save(user);
        return "redirect:/";
    }

    @PostMapping("/test-mail")
    public String testMail() throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        User user = users.get(0);
        if (user.email == null || user.email.isBlank()) {
            return "redirect:/user";
        }

        String subject = "【Trainbrella】通知機能テスト";
        String body =
                user.name + "様\n\n" +
                "ご利用ありがとうございます。\n" +
                "このメールは、遠距離通勤通学者向け天気・運行情報アプリ「Trainbrella(トレインブレラ)」の通知機能テストです。\n\n" +
                "メールが問題なく受信できた場合、通知機能は正常に動作しています。\n\n" +
                "設定されているメールアドレスは以下の通りです。\n" +
                user.email + "\n" +
                "メールが迷惑メールフォルダに振り分けられている場合は、メールアプリの受信設定を確認してください。\n" +
                "今後ともTrainbrellaをよろしくお願いいたします。";

        mailService.send(user.email, subject, body);
        return "redirect:/user";
    }
}
