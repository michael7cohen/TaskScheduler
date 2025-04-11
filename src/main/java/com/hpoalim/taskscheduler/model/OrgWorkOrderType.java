package com.hpoalim.taskscheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class OrgWorkOrderType {

    @JsonProperty("org")
    private String orgName;

    @JsonProperty("dataList")
    private List<WorkOrderType> workOrderTypes;
}
