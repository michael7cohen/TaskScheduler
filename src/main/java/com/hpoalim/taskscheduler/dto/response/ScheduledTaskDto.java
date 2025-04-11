package com.hpoalim.taskscheduler.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hpoalim.taskscheduler.model.Station;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScheduledTaskDto {
    private String operation;
    private Station station;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalDateTime endTime;
}
