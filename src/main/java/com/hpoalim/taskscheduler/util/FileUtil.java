package com.hpoalim.taskscheduler.util;

import com.hpoalim.taskscheduler.api.FileParseStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
public class FileUtil {

    public static <T> void parse(MultipartFile file, FileParseStrategy<T> strategy, Consumer<T> consumer) {
        validateParserInput(file, strategy, consumer);
        try {
            strategy.parse(file, consumer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse file: " + file.getOriginalFilename(), e);
        }
    }

    private static <T> void validateParserInput(MultipartFile file, FileParseStrategy<T> strategy, Consumer<T> consumer) {
        if (file == null || file.isEmpty()) {
            String error = "MultipartFile is empty or null, skipping parsing.";
            log.info(error);
            throw new RuntimeException(error);
        }

        if (strategy == null) {
            String error = "File parse strategy is null, skipping parsing.";
            log.info(error);
            throw new RuntimeException(error);
        }

        if (consumer == null) {
            String error = "Consumer is null, skipping parsing.";
            log.info(error);
            throw new RuntimeException(error);
        }
    }
}
