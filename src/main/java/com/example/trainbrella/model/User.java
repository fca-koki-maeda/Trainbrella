package com.example.trainbrella.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String name;
    public String fromCity;
    public String toCity;
    public String trainLine;
    public List<String> trainLines;
    public String email;

    // フォームバインド用（生リスト）
    public List<String> getTrainLines() {
        if (trainLines == null) trainLines = new ArrayList<>();
        return trainLines;
    }

    public void setTrainLines(List<String> trainLines) {
        this.trainLines = trainLines;
    }

    // 表示用（空白除去・最大5件）
    public List<String> getTrainLinesDisplay() {
        List<String> list = new ArrayList<>();
        if (trainLines != null) {
            for (String s : trainLines) {
                if (s != null && !s.trim().isEmpty()) list.add(s.trim());
            }
        }
        if (list.isEmpty() && trainLine != null && !trainLine.trim().isEmpty()) {
            list.add(trainLine.trim());
        }
        return list.size() > 5 ? list.subList(0, 5) : list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public String getTrainLine() {
        return trainLine;
    }

    public void setTrainLine(String trainLine) {
        this.trainLine = trainLine;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
