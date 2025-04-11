package com.hpoalim.taskscheduler.dto.response;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ScheduledTaskResponse {
    Map<String, List<ScheduledTaskDto>> scheduledTasks;

    public ScheduledTaskResponse() {
        this.scheduledTasks = new HashMap<>();
    }
}
