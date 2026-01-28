package com.example.trainbrella.controller;

import com.example.trainbrella.model.Record;
import com.example.trainbrella.model.RecordItem;
import com.example.trainbrella.model.User;
import com.example.trainbrella.service.RecordService;
import com.example.trainbrella.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/record")
public class RecordController {

    @Autowired RecordService service;
    @Autowired UserService userService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month,
                       @RequestParam(required = false) Integer day,
                       Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        model.addAttribute("user", users.get(0));

        List<Record> records = service.load();

        Map<String, List<RecordItem>> recordsByDate = new TreeMap<>(java.util.Collections.reverseOrder());
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < records.size(); i++) {
            Record r = records.get(i);
            String dateStr = r.dateTime != null ? r.dateTime.substring(0, 10) : r.date;
            if (dateStr == null) continue;

            try {
                LocalDate.parse(dateStr, dateFmt);
            } catch (Exception e) {
                continue;
            }
            recordsByDate.computeIfAbsent(dateStr, k -> new ArrayList<>())
                    .add(new RecordItem(i, r));
        }

        model.addAttribute("recordsByDate", recordsByDate);

        return "record";
    }

    @GetMapping("/new")
    public String newForm(Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        model.addAttribute("user", users.get(0));
        return "record_new";
    }

    @PostMapping
    public String add(@RequestParam String date,
                      @RequestParam String time,
                      @RequestParam String type,
                      @RequestParam String action,
                      @RequestParam(required = false) String condition,
                      @RequestParam(required = false) String delay,
                      @RequestParam(required = false) String memo) throws Exception {
        Record r = new Record();
        r.date = date;
        String safeTime = (time == null || time.isBlank()) ? "00:00" : time;
        r.dateTime = date + " " + safeTime + ":00";
        r.type = type;
        r.action = action;
        r.condition = condition;
        r.delayed = delay;
        r.memo = memo;

        service.add(r);
        return "redirect:/record";
    }

    @GetMapping("/edit/{index}")
    public String edit(@PathVariable int index, Model model) throws Exception {
        List<User> users = userService.load();
        if (users == null || users.isEmpty()) {
            return "redirect:/user";
        }
        List<Record> records = service.load();
        if (index < 0 || index >= records.size()) {
            return "redirect:/record";
        }
        model.addAttribute("user", users.get(0));
        model.addAttribute("record", records.get(index));
        model.addAttribute("index", index);
        return "record_edit";
    }

    @PostMapping("/edit")
    public String update(@RequestParam int index,
                         @RequestParam(required = false) String memo,
                         @RequestParam(required = false) String condition,
                         @RequestParam(required = false) String delayed) throws Exception {
        service.updateFields(index, memo, condition, delayed);
        return "redirect:/record";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam int index) throws Exception {
        service.delete(index);
        return "redirect:/record";
    }
}
