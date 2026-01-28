package com.example.trainbrella.service;

import com.example.trainbrella.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final Path FILE_PATH = Paths.get(System.getProperty("user.dir"), "data", "users.json");
    private final ObjectMapper mapper = new ObjectMapper();

    public List<User> load() {
        try {
            File file = FILE_PATH.toFile();
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return mapper.readValue(file, new TypeReference<List<User>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void save(User user) {
        try {
            List<User> users = load();
            users.clear(); // 今回は1ユーザーのみ
            users.add(user);

            Files.createDirectories(FILE_PATH.getParent());
            File file = FILE_PATH.toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, users);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
