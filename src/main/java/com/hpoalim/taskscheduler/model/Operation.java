package com.hpoalim.taskscheduler.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Operation {

    @JsonProperty("operation")
    private String name;

    private double durationHours;
}
