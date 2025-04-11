package com.hpoalim.taskscheduler.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hpoalim.taskscheduler.dto.response.ScheduledTaskDto;
import com.hpoalim.taskscheduler.dto.response.ScheduledTaskResponse;
import com.hpoalim.taskscheduler.model.*;
import com.hpoalim.taskscheduler.strategy.WorkOrderCsvStrategy;
import com.hpoalim.taskscheduler.util.FileUtil;
import com.hpoalim.taskscheduler.util.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class Test {

    private final Scheduler scheduler;

    public void createOrgStations(MultipartFile file) {
        OrgStations orgStations = JsonUtil.parseJsonFile(file, new TypeReference<OrgStations>() {});
        String org = orgStations.getOrg();
        List<Station> stations = orgStations.getStations();
        scheduler.addStations(org, stations);
    }

    public void createOrgWorkOrderTypes(MultipartFile file) {
        OrgWorkOrderType orgWorkOrderTypes = JsonUtil.parseJsonFile(file, new TypeReference<OrgWorkOrderType>() {});
        String org = orgWorkOrderTypes.getOrgName();
        List<WorkOrderType> workOrderTypes = orgWorkOrderTypes.getWorkOrderTypes();
        scheduler.addWorkOrderTypes(org, workOrderTypes);
    }

    public List<ScheduledTask> createWorkOrders(String org, MultipartFile file) {
        WorkOrderCsvStrategy csvStrategy = new WorkOrderCsvStrategy();
        List<WorkOrder> workOrders = new ArrayList<>();
        FileUtil.parse(file, csvStrategy.getStrategy(), workOrders::add);
        return scheduler.scheduleWorkOrders(org, workOrders);
    }

    public ScheduledTaskResponse getSchedule() {
        ScheduledTaskResponse response = new ScheduledTaskResponse();
        Map<String, List<ScheduledTask>> scheduledTasks = scheduler.getScheduledTasks();
        scheduledTasks.forEach((org, tasks) -> {
            List<ScheduledTaskDto> scheduledTasksDto = new ArrayList<>();
            for (ScheduledTask task : tasks) {
                ScheduledTaskDto dto = new ScheduledTaskDto();
                dto.setOperation(task.getOperation());
                dto.setStation(task.getStation());
                dto.setStartTime(task.getStartTime());
                dto.setEndTime(task.getEndTime());
                scheduledTasksDto.add(dto);
            }
            response.getScheduledTasks().put(org, scheduledTasksDto);
        });

        return response;
    }
}
