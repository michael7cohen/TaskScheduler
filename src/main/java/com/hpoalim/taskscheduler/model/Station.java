package com.hpoalim.taskscheduler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.AllArgsConstructor;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Station {
    private String name;
    private String operation;
    private int capacity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return capacity == station.capacity &&
                Objects.equals(name, station.name) &&
                Objects.equals(operation, station.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, operation, capacity);
    }
}
