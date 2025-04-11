## Work Order Scheduling – Spring Boot
This project implements a scheduling service that coordinates Work Orders, Stations, and Work Order Types. 
It ensures that each work order’s operations are assigned to the correct stations, respecting capacity and work-day limits.

## Project Overview
- Stations: Represent resources or operation centers (e.g., Cutting, Layup), each with a set capacity (how many tasks can run in parallel).

- Work Order Types: Define a sequence of operations (name and duration) that must be followed strictly when completing a work order.

- Work Orders: Actual tasks that reference a specific type (which operations to perform) and have a due date.

The scheduler uses a **forward scheduling** method:

1. Sorts work orders by due date (or some priority).

2. Iterates over each work order’s operations in order.

3. Assigns each operation to the station that supports it.

4. If there isn’t enough time or capacity in the current day, scheduling continues on the next day (a naive approach).

## Features
- **Multiple Stations per Organization**
Each organization (identified by org) can have multiple stations, each supporting a specific operation.

- **Dependency Management**
Work orders are processed in the order dictated by their type’s operation list.

- **Capacity Constraints**
The scheduler ensures station capacity is never exceeded. If capacity is full, the task is deferred to another timeslot or day.

- **Daily Work Window**
The system enforces a daily start/end time (e.g., 07:00-16:00).

- **Naive Forward Scheduling**
If operations can’t be fitted into the same day, the scheduler shifts them to the next available day.


## Endpoints
POST http://localhost:8080/api/createStation

- Description: Create or add stations for a given organization.
- **stations.json:**

```json
{
  "org": "myOrg",
  "stations": [
    {
      "name": "CuttingStation",
      "operation": "Cutting",
      "capacity": 2
    },
    {
      "name": "Oven",
      "operation": "Cure",
      "capacity": 3
    }
  ]
}
```

```bash
curl -X POST -F "file=@ <your path>/stations.json" http://localhost:8080/api/createStation
```
- **Notes:** Must be called before scheduling work orders that require these stations.

2. POST http://localhost:8080/api/createWorkOrderTypes

- Description: Create a list of work order types (the “recipes” of operations).

- **work_order_types.json:**

```json
{
  "org": "myOrg",
  "dataList": [
    {
      "name": "type1",
      "operations": [
        {
          "name": "Cutting",
          "durationHours": 2
        },
        {
          "name": "Layup",
          "durationHours": 8
        }
      ]
    },
    {
      "name": "type2",
      "operations": [
        {
          "name": "Cutting",
          "durationHours": 1.5
        },
        {
          "name": "Cooling",
          "durationHours": 4
        }
      ]
    }
  ]
}
```

```bash
curl -X POST -F "file=@ <your path>/work_order_types.json" \
     http://localhost:8080/api/createWorkOrderTypes
```

- **Notes:** Must be defined before uploading any work orders referencing these types.

3. POST http://localhost:8080/api/uploadWorkOrder/{org}

- Description: Uploads a list of work orders for a specific organization.

- Path Variable: {org} – organization ID.

- Body (JSON or CSV):

``` csv
name,type,dueDate
workOrder1,type1,2025-04-01T10:00:00
workOrder2,type2,2025-04-02T12:00:00
workOrder3,type3,2025-04-02T12:00:00
```

```bash
curl -X POST -F "file=@ <your path>/work_orders.csv" \
http://localhost:8080/api/uploadWorkOrder/myOrg
```

- **Notes:** The type must match one of the previously created types.

- **GET** http://localhost:8080/api/schedule

- **Description:** Executes the scheduling algorithm for all work orders that have been uploaded, returning a list of scheduled tasks.

- **Sample Response:**
```json
{
  "scheduledTasks": {
    "Test": [
      {
        "operation": "Cutting",
        "station": {
          "name": "Cutting",
          "operation": "Cutting",
          "capacity": 2
        },
        "startTime": "07:00:00",
        "endTime": "08:30:00"
      },
      {
        "operation": "Cutting",
        "station": {
          "name": "Cutting",
          "operation": "Cutting",
          "capacity": 2
        },
        "startTime": "07:00:00",
        "endTime": "08:30:00"
      }
    ]
  }
}
```
```bash
curl -X GET http://localhost:8080/api/schedule
```
- **Notes:** The scheduler processes each organization’s work orders if stations and work orders are available.


## Recommended Call Order
1. **POST** http://localhost//api/createStation
2. **POST** http://localhost/api/createWorkOrderTypes 
3. **POST** http://localhost/api/uploadWorkOrder/{org}
4. **GET**  http://localhost/api/schedule

**Important:** The creation of stations and work order types must precede the uploading of work orders. Only after uploading work orders should you call /api/schedule to run the scheduling process.

## How to Run
1. Clone the repository and import into your IDE (e.g., IntelliJ, Eclipse).

2. Configure any required properties (e.g., application.properties for scheduler.workDayStart and scheduler.workDayEnd).

3. Build and run (e.g., mvn spring-boot:run or from your IDE).

4. Test the endpoints with a REST client or browser (e.g., Postman or cURL).

## Future Enhancements
- **Advanced Scheduling Algorithms:** Incorporate more sophisticated heuristics or optimization solvers (OptaPlanner, OR-Tools).

- **Partial-Day Scheduling:** Allow splitting a long operation across multiple days.

- **Real-Time Updates:** Integrate a messaging system (WebSockets or Kafka) to provide real-time station capacity feedback.

- **Database Support:** Persist stations, work orders, and schedules in a relational or NoSQL database for better scalability.
