# OpenMRS Module Appointments Backend

This repository acts as the back end for the **Bahmni Appointment Scheduling**.

## License

[MPL 2.0 w/ HD](http://openmrs.org/license/) © [OpenMRS Inc](http://www.openmrs.org/).

For further details, refer [OpenMRS License FAQ](https://wiki.openmrs.org/display/RES/OpenMRS+License+FAQ).
## Packaging

`mvn clean package`

The output is the OMOD file:
`openmrs-module-appointments/omod/target/appointments-[VERSION].omod`

# Setup instructions
### Note
This module is incompatible with the openmrs-appointment-scheduling module.
If you already have "openmrs-appointment-scheduling" module installed, please do any of the following for a smooth setup.

    1. Uninstall the "openmrs-appointment-scheduling" module from the openmrs admin page
    2. Identify openmrs modules directory, and remove the module file from there.
You can execute the below command in the terminal to remove the module from OpenMRS module directory:

```
sudo rm /path/to/modules/directory/appointmentscheduling-1.13.0.omod
```

You may optionally choose to remove the database tables specific to openmrs-appointment-scheduling module. The following SQL statements will drop the tables.
```
set foreign_key_checks=0;
drop table if exists appointmentscheduling_appointment;
drop table if exists appointmentscheduling_appointment_block;
drop table if exists appointmentscheduling_appointment_request;
drop table if exists appointmentscheduling_appointment_status_history;
drop table if exists appointmentscheduling_appointment_type;
drop table if exists appointmentscheduling_block_type_map;
drop table if exists appointmentscheduling_provider_schedule;
drop table if exists appointmentscheduling_provider_type_map;
drop table if exists appointmentscheduling_time_slot;
set foreign_key_checks=1;
```

# **Clinical Appointments Scheduling**

# **About**

This module aims to support users to create and track patient appointments. It supports creation, editing, canceling, and synchronization of appointments.

# **Appointments module tables**

Below is a diagram to show a schema of the appointments scheduling module.

![alt_text](https://drive.google.com/uc?export=view&id=1SjbUBRPScmOzhB8YEruM1IX6tbSY5a55 "image_tooltip")

# **_Features currently supported_**

- Creating appointments.
- Edit appointments.
- Update appointments.
- Synchronize appointments.
- Cancel appointments.


# REST documentation

> Not the ideal place to have this document.
> N.B in the documentation replace the parameters with your values e.g ?forDate=2108-08-15T00:00:00.0Z should be user supplied

### Appointments resource

1. #### Get all appointments

Retrieves all appointments. Returns a `401` status code if not authenticated.

```http request
 GET /ws/rest/v1/appointment/all
```

2. #### Get all appointments by date

Retrieves all appointments filtered by a specified date.

```http request
GET /ws/rest/v1/appointment/all?forDate=2108-08-15T00:00:00.0Z
```

| Parameter | Type   | Description                    |
| :-------- | :----- | :----------------------------- |
| `forDate` | `date` | Date to filter appointment for |

3. #### Get all appointments by date and status

Retrieves all appointments filtered by a specified date.

```http request
GET /ws/rest/v1/appointment/all?forDate=2108-08-15T00:00:00.0Z&status=missed
```

| Parameter | Type     | Description                    |
| :-------- | :------- | :----------------------------- |
| `forDate` | `date`   | Date to filter appointment for |
| `status`  | `string` | Status of the appointment      |

4. ### Get all appointments by service uuid

Retrieves all appointments by service uuid

```http request
POST ws/rest/v1/appointment/search
```

body

```json
{
  "serviceUuid": "c36006e5-9fbb-4f20-866b-0ece245615a6"
}
```

5. ### Create patient appointment

Create an appointment for a patient. Returns error `404` status code if payload doesn't match the model. `500` if there is an internal error code. `200` if request is succesful.

```http request
POST /ws/rest/v1/appointment
```

body

```json
{
  "providerUuid": "823fdcd7-3f10-11e4-adec-0800271c1b75",
  "appointmentNumber": "1",
  "patientUuid": "2c33920f-7aa6-48d6-998a-60412d8ff7d5",
  "serviceUuid": "c36006d4-9fbb-4f20-866b-0ece245615c1",
  "startDateTime": "2017-07-20",
  "endDateTime": "2017-07-20",
  "appointmentKind": "WalkIn",
  "providers": [
    {
      "uuid": "2d15071d-439d-44e8-9825-aa8e1a30d2a2",
      "comments": "available",
      "response": "ACCEPTED"
    }
  ]
}
```

6. ### Get all non-cancelled, non-voided future appointments by appointment service type

Retrieves all valid future appointments by appointment service type

```http request
GET /ws/rest/v1/appointment/futureAppointmentsForServiceType?appointmentServiceTypeUuid=678906e5-9fbb-4f20-866b-0ece24564578
```

| Parameter                    | Type     | Description                   |
| :--------------------------- | :------- | :---------------------------- |
| `appointmentServiceTypeUuid` | `string` | appointment service type uuid |

7. ### Get all appointment services

Retrieves all appointment services.

```http request
GET /ws/rest/v1/appointmentService/all/default
```

response

```json
{
        "appointmentServiceId": 2,
        "name": "HIV follow up",
        "description": "Simple HIV Visit",
        "speciality": {},
        "startTime": "09:00:00",
        "endTime": "17:30:00",
        "maxAppointmentsLimit": 30,
        "durationMins": 30,
        "location": {},
        "uuid": "99b2ea38-e041-41eb-9ed2-25a265068764",
        "color": "#00ff00",
        "initialAppointmentStatus": null,
        "creatorName": null
    },
```

8. Get appointment service by uuid

Retrieves an appointment service by uuid

```http request
GET /ws/rest/v1/appointmentService?uuid=some-uuid
```

| Parameter | Type     | Description              |
| :-------- | :------- | :----------------------- |
| `uuid`    | `string` | appointment service uuid |

9. Create appointment service

Creates a new appointment service. returns status code `404` if request body doesn't match model requirements. return status code `201` if successful.

```http request
POST /ws/rest/v1/appointmentService
```

body

```json
{
  "name": "Cardiology Consultation",
  "startTime": "09:00:00",
  "endTime": "17:30:00",
  "durationMins": "30",
  "locationUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
  "specialityUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
  "maxAppointmentsLimit": "30",
  "color": "#00ff00"
}
```

This request body can work with the following cases

- Create appointment with name only
  ```json
  {
    "name": "Sample Appointment Servie"
  }
  ```
- Create with service availability
  ```json
  {
    "name": "Cardiology Consultation",
    "startTime": "09:00:00",
    "endTime": "17:30:00",
    "durationMins": "30",
    "locationUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
    "specialityUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
    "maxAppointmentsLimit": "30",
    "color": "#0000ff",
    "weeklyAvailability": [
      {
        "dayOfWeek": "MONDAY",
        "startTime": "09:00:00",
        "endTime": "17:30:00",
        "maxAppointmentsLimit": "10"
      }
    ]
  }
  ```
- Create with service types
  ```json
  {
    "name": "Cardiology Consultation",
    "startTime": "09:00:00",
    "endTime": "17:30:00",
    "durationMins": "30",
    "color": "#fff000",
    "locationUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
    "specialityUuid": "c36006e5-9fbb-4f20-866b-0ece245615a1",
    "serviceTypes": [
      {
        "name": "type1",
        "duration": "20"
      }
    ]
  }
  ```
- Create with initial appointment status
  ```json
  {
    "name": "Cardiology Consultation",
    "initialAppointmentStatus": "Requested"
  }
  ```

10. ### Get count of appointments by service type for a date range

Retrieves the count of appointments based on service type for a date range

```http request
GET /ws/rest/v1/appointment/appointmentSummary?startDate=2022-08-02T00:00:00.0Z&endDate=2022-08-02T00:00:00.0Z
```

| Parameter   | Type   | Description             |
| :---------- | :----- | :---------------------- |
| `startDate` | `date` | Date to start filtering |
| `endDate`   | `date` | Date to stop filtering  |

11. ### Get appointment service load for a date range.

Retrieves the number of appointment for a date range. This is close to a distrubtion calendar of appointments

```http request
GET /ws/rest/v1/appointmentService/load?uuid=99b2ea38-e041-41eb-9ed2-25a265068764&startDateTime=&endDateTime=
```

| Parameter   | Type     | Description              |
| :---------- | :------- | :----------------------- |
| `uuid`      | `string` | appointment service uuid |
| `startDate` | `date`   | Date to start filtering  |
| `endDate`   | `date`   | Date to stop filtering   |
