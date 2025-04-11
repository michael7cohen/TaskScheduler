package com.hpoalim.taskscheduler.server;

import com.hpoalim.taskscheduler.model.Operation;
import com.hpoalim.taskscheduler.model.ScheduledTask;
import com.hpoalim.taskscheduler.model.Station;
import com.hpoalim.taskscheduler.model.WorkOrder;
import com.hpoalim.taskscheduler.model.WorkOrderType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Scheduler Service - supports scheduling tasks for different organizations ("org").
 *
 * <p><strong>stationsMap</strong>: A map of org -> (operationName -> Station).
 * <br><strong>WorkOrderTypeMap</strong>: A map of org -> (workOrderTypeName -> List of operations).
 * <br><strong>scheduleMap</strong>: A map of org -> (operationName -> (LocalDate -> List of scheduled tasks)).
 *
 * <p>The scheduler uses a naive forward-scheduling approach:
 * when a station is unavailable or there's not enough time in the current day,
 * it moves on to the next day and tries to schedule again.
 */
@Slf4j
@Service
public class Scheduler {

    /**
     * Defines the daily start time of the working day (e.g. 07:00).
     */
    private final LocalTime WORK_DAY_START;

    /**
     * Defines the daily end time of the working day (e.g. 16:00).
     */
    private final LocalTime WORK_DAY_END;

    /**
     * A map of organization ID -> (operationName -> Station)
     */
    private final Map<String, Map<String, Station>> stationsMap;

    /**
     * A map of organization ID -> (workOrderTypeName -> List<Operation>)
     */
    private final Map<String, Map<String, List<Operation>>> WorkOrderTypeMap;

    /**
     * A schedule map where:
     * key (1) = org (String),
     * key (2) = operationName (String),
     * value   = Map of (LocalDate -> List<ScheduledTask>).
     */
    private final Map<String, Map<String, Map<LocalDate, List<ScheduledTask>>>> scheduleMap;

    /**
     * Constructs the Scheduler with a daily start/end time.
     * The maps for stations, work order types, and scheduled tasks
     * are initialized to empty structures.
     *
     * @param workDayStart the daily start time as a string (e.g. "07:00")
     * @param workDayEnd the daily end time as a string (e.g. "16:00")
     */
    public Scheduler(
            @Value("${scheduler.workDayStart}") String workDayStart,
            @Value("${scheduler.workDayEnd}")   String workDayEnd
    ) {
        this.WORK_DAY_START = LocalTime.parse(workDayStart);
        this.WORK_DAY_END   = LocalTime.parse(workDayEnd);

        this.stationsMap = new HashMap<>();
        this.WorkOrderTypeMap = new HashMap<>();
        this.scheduleMap = new HashMap<>();
    }

    /**
     * Adds a list of stations for a specific organization.
     *
     * @param customerId the organization ID
     * @param stations   the list of stations to add
     */
    public void addStations(String customerId, List<Station> stations) {
        if (customerId == null || customerId.isEmpty()) {
            throw new IllegalArgumentException("customerId cannot be null or empty");
        }
        if (stations == null || stations.isEmpty()) {
            throw new IllegalArgumentException("Stations list cannot be null or empty");
        }

        for (Station station : stations) {
            addStation(customerId, station);
        }
    }

    /**
     * Adds a single station to a specific organization.
     *
     * @param org     the organization ID
     * @param station the station to add
     */
    private void addStation(String org, Station station) {
        if (StringUtils.isEmpty(org)) {
            throw new IllegalArgumentException("customerId cannot be null or empty");
        }

        String operation = station.getOperation();
        stationsMap.computeIfAbsent(org, k -> new HashMap<>()).put(operation, station);
        scheduleMap.computeIfAbsent(org, k -> new HashMap<>()).put(operation, new HashMap<>());
    }

    /**
     * Adds multiple WorkOrderTypes for a given organization.
     *
     * @param org            the organization ID
     * @param workOrderTypes list of WorkOrderType objects
     */
    public void addWorkOrderTypes(String org, List<WorkOrderType> workOrderTypes) {
        if (org == null || org.isEmpty()) {
            throw new IllegalArgumentException("org cannot be null or empty");
        }
        if (workOrderTypes == null || workOrderTypes.isEmpty()) {
            throw new IllegalArgumentException("WorkOrderTypes list cannot be null or empty");
        }

        for (WorkOrderType workOrderType : workOrderTypes) {
            addWorkOrderType(org, workOrderType);
        }
    }

    /**
     * Adds a single WorkOrderType for a given organization.
     *
     * @param org           the organization ID
     * @param workOrderType the WorkOrderType to add
     */
    public void addWorkOrderType(String org, WorkOrderType workOrderType) {
        if (org == null || org.isEmpty()) {
            throw new IllegalArgumentException("org cannot be null or empty");
        }

        Map<String, List<Operation>> operationMap = WorkOrderTypeMap.computeIfAbsent(org, k -> new HashMap<>());
        String name = workOrderType.getName();
        List<Operation> operations = workOrderType.getOperations();

        if (operations == null || operations.isEmpty()) {
            operationMap.remove(org);
            return;
        }

        operationMap.computeIfAbsent(name, k -> new ArrayList<>()).addAll(operations);
    }

    /**
     * Schedules a list of WorkOrders for a specific organization.
     * <ol>
     *   <li>Sorts the WorkOrders by their due date.</li>
     *   <li>For each WorkOrder, iterates over the Operations defined by its type in order.</li>
     *   <li>For each Operation, attempts to schedule on the relevant station at the earliest possible time.</li>
     *   <li>If it cannot be placed on the same day, it moves to the next day (naive logic).</li>
     * </ol>
     *
     * @param org        the organization ID
     * @param workOrders the list of WorkOrders
     * @return a list of ScheduledTask objects representing the final schedule
     */
    public List<ScheduledTask> scheduleWorkOrders(String org, List<WorkOrder> workOrders) {
        if (org == null || org.isEmpty()) {
            throw new IllegalArgumentException("org cannot be null or empty");
        }
        if (!stationsMap.containsKey(org)) {
            throw new IllegalStateException("No stations defined for org=" + org);
        }

        List<ScheduledTask> result = new ArrayList<>();

        // Sort WorkOrders by due date
        workOrders.sort(Comparator.comparing(WorkOrder::getDueDate));

        Map<String, Station> orgStationMap = stationsMap.get(org);
        Map<String, List<Operation>> orgOperationMap = WorkOrderTypeMap.get(org);

        if (orgOperationMap == null || orgOperationMap.isEmpty()) {
            throw new IllegalStateException("No operations defined for org=" + org);
        }

        for (WorkOrder wo : workOrders) {
            // Earliest time to start the first operation: (for demonstration, "today" at WORK_DAY_START)
            LocalDateTime currentEarliestStart = LocalDate.now().atTime(WORK_DAY_START);

            List<Operation> operations = orgOperationMap.get(wo.getType());
            if (operations == null || operations.isEmpty()) {
                log.warn("No operations for work order type: {} for org {}", wo.getType(), org);
                continue;
            }

            // Schedule each operation in the order defined by the WorkOrderType
            for (Operation od : operations) {
                currentEarliestStart = addOperationToSchedule(
                        org, wo, od, orgStationMap, currentEarliestStart, result
                );
            }
        }
        return result;
    }

    /**
     * Helper method to schedule a single operation and return the updated earliest start time.
     */
    private LocalDateTime addOperationToSchedule(String org,
                                                 WorkOrder workOrder,
                                                 Operation operation,
                                                 Map<String, Station> orgStationMap,
                                                 LocalDateTime currentEarliestStart,
                                                 List<ScheduledTask> result) {

        String operationName = operation.getName();
        Station station = orgStationMap.get(operationName);
        if (station == null) {
            log.warn("No station for operation: {} for org {}", operationName, org);
            throw new IllegalStateException("No station for operation: " + operationName + " for org " + org);
        }

        int durationMinutes = (int) Math.ceil(operation.getDurationHours() * 60);

        // Attempt to schedule the task for this station
        ScheduledTask st = scheduleTaskForStation(org, workOrder, station, currentEarliestStart, durationMinutes);
        result.add(st);

        // Add to the schedule map so we keep track of assigned tasks
        addToScheduleMap(org, st);

        // Return the end time as the earliest start time for the next operation
        return st.getEndTime();
    }

    /**
     * Attempts to schedule a task (an Operation in a WorkOrder) on a suitable station.
     * Starts from earliestStart, requiring durationMinutes of continuous time.
     * Returns a ScheduledTask if successful, otherwise throws an exception.
     */
    private ScheduledTask scheduleTaskForStation(
            String customerId,
            WorkOrder wo,
            Station station,
            LocalDateTime earliestStart,
            int durationMinutes
    ) {
        LocalDate day  = earliestStart.toLocalDate();
        LocalTime time = earliestStart.toLocalTime();

        while (true) {
            // If time is before the workday start, align it
            if (time.isBefore(WORK_DAY_START)) {
                time = WORK_DAY_START;
            }
            // If time is after the workday end, move to the next day
            if (time.isAfter(WORK_DAY_END)) {
                day  = day.plusDays(1);
                time = WORK_DAY_START;
            }

            // Calculate how many minutes remain in the current day
            int minutesUntilDayEnd = (int) Duration.between(
                    LocalDateTime.of(day, time),
                    LocalDateTime.of(day, WORK_DAY_END)
            ).toMinutes();

            // If not enough time in this day, move to the next day
            if (minutesUntilDayEnd < durationMinutes) {
                day  = day.plusDays(1);
                time = WORK_DAY_START;
                continue;
            }

            // Check capacity on the station in the time range
            if (canFit(customerId, station, day, time, durationMinutes)) {
                // We can schedule
                LocalDateTime start = LocalDateTime.of(day, time);
                LocalDateTime end   = start.plusMinutes(durationMinutes);

                return new ScheduledTask(wo, station.getOperation(), station, start, end);
            } else {
                // Not enough capacity; try the next day (naive approach)
                day  = day.plusDays(1);
                time = WORK_DAY_START;
            }
        }
    }

    /**
     * Checks if a task can fit in the station's schedule (for a given org),
     * on a specific date and time range, without exceeding the station's capacity.
     */
    private boolean canFit(String org,
                           Station station,
                           LocalDate day,
                           LocalTime startTime,
                           int durationMinutes) {

        // Retrieve all tasks already scheduled for this station on the given date
        Map<String, Map<LocalDate, List<ScheduledTask>>> stationLevelMap = scheduleMap.get(org);
        String operation = station.getOperation();
        Map<LocalDate, List<ScheduledTask>> dayMap = stationLevelMap.get(operation);
        if (dayMap == null) {
            throw new IllegalStateException("No schedule for station: " + operation + " for org " + org);
        }
        List<ScheduledTask> tasks = dayMap.computeIfAbsent(day, d -> new ArrayList<>());

        LocalDateTime start = LocalDateTime.of(day, startTime);

        // Check each minute in [start, start + durationMinutes) for overlapping tasks
        for (int m = 0; m < durationMinutes; m++) {
            LocalDateTime checkTime = start.plusMinutes(m);
            int overlapping = 0;

            for (ScheduledTask t : tasks) {
                if (isOverlapping(checkTime, t)) {
                    overlapping++;
                    if (overlapping >= station.getCapacity()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines whether the checkTime moment overlaps with a given scheduled task.
     * Overlap if checkTime >= task.startTime and checkTime < task.endTime.
     */
    private boolean isOverlapping(LocalDateTime checkTime, ScheduledTask t) {
        return !checkTime.isBefore(t.getStartTime()) && checkTime.isBefore(t.getEndTime());
    }

    /**
     * Adds a scheduled task to the schedule map for the specified organization.
     */
    private void addToScheduleMap(String customerId, ScheduledTask task) {
        Map<String, Map<LocalDate, List<ScheduledTask>>> stationLevelMap = scheduleMap.get(customerId);
        stationLevelMap.putIfAbsent(task.getStation().getName(), new HashMap<>());

        Map<LocalDate, List<ScheduledTask>> dayMap = stationLevelMap.get(task.getStation().getName());
        LocalDate day = task.getStartTime().toLocalDate();

        dayMap.computeIfAbsent(day, d -> new ArrayList<>()).add(task);
    }

    public Map<String, List<ScheduledTask>> getScheduledTasks() {
        Map<String, List<ScheduledTask>> result = new HashMap<>();
        scheduleMap.forEach((org, operationMap) -> {
            operationMap.forEach((operation, dayMap) -> {
                dayMap.forEach((day, tasks) -> {
                    result.putIfAbsent(org, new ArrayList<>());
                    result.get(org).addAll(tasks);
                });
            });
        });
        return result;
    }
}
