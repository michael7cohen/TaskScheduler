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
public class OrgStations {

    private String org;

    @JsonProperty("dataList")
    private List<Station> stations;
}
