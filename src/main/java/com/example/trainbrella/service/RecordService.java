package com.example.trainbrella.service;

import com.example.trainbrella.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.util.*;

@Service
public class RecordService {

    private final Path path = Paths.get("data/records.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public List<Record> load() throws Exception {
        if (!Files.exists(path)) return new ArrayList<>();
        return Arrays.asList(mapper.readValue(path.toFile(), Record[].class));
    }

    public void add(Record r) throws Exception {
        List<Record> list = new ArrayList<>(load());
        list.add(r);
        mapper.writeValue(path.toFile(), list);
    }

    public void upsertTodayRecord(Record r, String today) throws Exception {
        List<Record> list = new ArrayList<>(load());
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            Record cur = list.get(i);
            boolean isToday = (cur.dateTime != null && cur.dateTime.startsWith(today))
                    || (cur.date != null && cur.date.equals(today));
            if (isToday && Objects.equals(cur.type, r.type) && Objects.equals(cur.action, r.action)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            list.set(index, r);
        } else {
            list.add(r);
        }
        mapper.writeValue(path.toFile(), list);
    }

    public void updateFields(int index, String memo, String condition, String delayed) throws Exception {
        List<Record> list = new ArrayList<>(load());
        if (index < 0 || index >= list.size()) return;
        Record r = list.get(index);
        r.memo = memo;
        r.condition = condition;
        r.delayed = delayed;
        mapper.writeValue(path.toFile(), list);
    }

    public void delete(int index) throws Exception {
        List<Record> list = new ArrayList<>(load());
        if (index < 0 || index >= list.size()) return;
        list.remove(index);
        mapper.writeValue(path.toFile(), list);
    }
}
