package com.hpoalim.taskscheduler.strategy;

import com.hpoalim.taskscheduler.api.FileParseStrategy;
import com.hpoalim.taskscheduler.model.WorkOrder;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static com.hpoalim.taskscheduler.util.DateUtil.DATE_FORMATTER;

/**
 * This class provides a strategy for parsing work order data from a CSV file.
 * It implements the FileParseStrategy interface to define how to read and process the CSV file.
 */
@Slf4j
public class WorkOrderCsvStrategy {

    private final int ID_INDEX = 0;
    private final int TYPE_INDEX = 1;
    private final int DATE_INDEX = 2;

    public FileParseStrategy<WorkOrder> getStrategy() {
        return (file, consumer) -> {
            try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(file.getInputStream())))) {
                String[] line;
                reader.readNext();
                while ((line = reader.readNext()) != null) {
                    String id = line[ID_INDEX];
                    String type = line[TYPE_INDEX];
                    LocalDate dueDate = LocalDate.parse(line[DATE_INDEX], DATE_FORMATTER);
                    WorkOrder workOrder = WorkOrder.builder()
                            .id(id)
                            .type(type)
                            .dueDate(dueDate)
                            .build();
                    consumer.accept(workOrder);
                }
            } catch (CsvValidationException | IOException e) {
                log.error("Error reading CSV file", e);
                throw new RuntimeException("Failed to parse CSV file: " + file.getOriginalFilename(), e);
            }
        };
    }
}
