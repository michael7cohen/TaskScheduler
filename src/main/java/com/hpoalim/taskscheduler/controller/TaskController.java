package com.hpoalim.taskscheduler.controller;

import com.hpoalim.taskscheduler.dto.response.ScheduledTaskResponse;
import com.hpoalim.taskscheduler.model.ScheduledTask;
import com.hpoalim.taskscheduler.server.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TaskController {

    private TaskService taskService;


    @GetMapping(value = "/schedule", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ScheduledTaskResponse> schedule() {
        try {
            return ResponseEntity.ok(taskService.getSchedule());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping(value = "/uploadWorkOrder/{org}", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ScheduledTask>> uploadWorkOrder(
            @PathVariable("org") @Valid @NotEmpty String org,
            @RequestPart("file") @Valid @NotNull MultipartFile file) {
        try {
            return ResponseEntity.ok(taskService.createWorkOrders(org, file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping(value = "/createStation", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createStation(@RequestPart("file") @Valid @NotNull MultipartFile file) {
        try {
            taskService.createOrgStations(file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error raising the file:" + file.getName());
        }
    }

    @PostMapping(value = "/createWorkOrderTypes", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createWorkOrderTypes(@RequestPart("file") @Valid @NotNull MultipartFile file) {
        try {
            taskService.createOrgWorkOrderTypes(file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error raising the file:" + file.getName());
        }
    }
}
