package com.hpoalim.taskscheduler.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T parseJsonFile(MultipartFile file, TypeReference<T> typeReference) {
        if (file == null || file.isEmpty()) {
            String error = "MultipartFile is empty or null, skipping parsing.";
            throw new RuntimeException(error);
        }
        try (InputStream inputStream = file.getInputStream()) {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON file: " + file.getOriginalFilename(), e);
        }
    }
}
