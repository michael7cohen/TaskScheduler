package com.hpoalim.taskscheduler.api;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.function.Consumer;

@FunctionalInterface
public interface FileParseStrategy<T> {
    void parse(MultipartFile file, Consumer<T> consumer) throws IOException;
}
