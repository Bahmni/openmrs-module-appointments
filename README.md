# OpenMRS Module Appointments Backend

This repository acts as the back end for the **Bahmni Appointment Scheduling**.

## Packaging

`mvn clean package`

The output is the OMOD file:
`openmrs-module-appointments/omod/target/appointments-[VERSION].omod`

# Setup instructions

This module is incompatiable with the default openmrs-appointment-scheduling module, please ensure you remove it either from the admin-page or running this command below from where your modules are located in tomcat directory i.e `/var/lib/OpenMRS/modules` on linux system

```
sudo rm appointmentscheduling-1.13.0.omod
```

If you get database error while running the module, remove this tables below. by running the following sql statements.

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

The appointment scheduling module aims at supporting 3.x users in creating and tracking patient appointments in order to do proper planning and follow up for the services that are being offered in a clinical set-up.

It will also support some other actions; Edit, Cancel and Synchronize appointments.

```openmrs-module-appointments/omod/target/appointments-[VERSION].omod```

# Setup instructions

This module is incompatiable with the default openmrs-appointment-scheduling module, please ensure you remove it either from the admin-page or running this command below from where your modules are located in tomcat directory i.e `/var/lib/OpenMRS/modules` on linux system

```
sudo rm appointmentscheduling-1.13.0.omod
```

If you get database error while running the module, remove this tables below. by running the following sql statements.

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

The appointment scheduling module aims at supporting 3.x users in creating and tracking patient appointments in order to do proper planning and follow up for the services that are being offered in a clinical set-up.

It will also support some other actions; Edit, Cancel and Synchronize appointments.

# **Where to find it**

The widget will be available on the left nav home page and it will be named **Clinical Appointments**.

# **Appointments module schema**

Below is a diagram to show a schema of the appointments scheduling module.

![alt_text](https://drive.google.com/uc?export=view&id=1SjbUBRPScmOzhB8YEruM1IX6tbSY5a55 "image_tooltip")

# **_What does the appointments scheduling module entail?_**

- Appointments summary dashboard.
- Creating appointments.
- Edit appointments.
- Update appointments.
- Synchronize appointments.
- Cancel appointments.

# **_Clinical appointments summary dashboard._**

On this summary dashboard, users can view the daily appointments patient lists with the various outcomes of the appointments lists on different tabs, i.e. booked appointments, missed appointments, completed and canceled appointments.

![alt_text](https://drive.google.com/uc?export=view&id=1HMM2k_hD9z2FaPh0kdlClVcHwsVsWrhH "image_tooltip")

**_Booked appointments_**

Shows a patient list of appointments and service(s) booked for the day.

## **_Missed appointments_**

Shows a patient line list of appointments with service(s) that were missed for the day.

## **Completed appointments**

Shows a patient line list of appointments and service(s) that were honored for the day.

## **Canceled appointments**

Shows a patient line list of patients who have had their appointments canceled.

# **_Add/Create appointments._**

By use of a form, users will be able to create appointments at the patient chart level by clicking on the Add New appointment button, search for a patient and open the appointments form. \
User will then attach the appointment to a service and save the appointments. There are multiple sections in the form that are must-fill before the form can be saved.

This action would then add the patient to the _Clinical appointments dashboard_ under the _booked appointments_ tab for the given day.

![alt_text](https://drive.google.com/uc?export=view&id=1S6zuvVG9TbwkKJos8EUl2zmMC7KUTcvs "image_tooltip")

**_Edit appointments_**

A user will be able to edit appointments by using the actions functions against each patient on the lists which will open the appointments form in edit mode to allow users to update the appointment details.

# **_Sync appointments_**

The module will support syncing of related appointments e.g. a case where a pair of a mother and baby need to be given the same appointment.

# **_Cancel appointments_**

A user will be able to cancel an appointment in a scenario where a patient has communicated that they would not be attending their scheduled appointment. The cancel function will be available against each appointment and this action would move the patient into the _Canceled appointments_ patient list.

# rest documentation

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

![alt_text](https://drive.google.com/uc?export=view&id=1SjbUBRPScmOzhB8YEruM1IX6tbSY5a55 "image_tooltip")

# **_What does the appointments scheduling module entail?_**

- Appointments summary dashboard.
- Creating appointments.
- Edit appointments.
- Update appointments.
- Synchronize appointments.
- Cancel appointments.

# **_Clinical appointments summary dashboard._**

On this summary dashboard, users can view the daily appointments patient lists with the various outcomes of the appointments lists on different tabs, i.e. booked appointments, missed appointments, completed and canceled appointments.

![alt_text](https://drive.google.com/uc?export=view&id=1HMM2k_hD9z2FaPh0kdlClVcHwsVsWrhH "image_tooltip")

**_Booked appointments_**

Shows a patient list of appointments and service(s) booked for the day.

## **_Missed appointments_**

Shows a patient line list of appointments with service(s) that were missed for the day.

## **Completed appointments**

Shows a patient line list of appointments and service(s) that were honored for the day.

## **Canceled appointments**

Shows a patient line list of patients who have had their appointments canceled.

# **_Add/Create appointments._**

By use of a form, users will be able to create appointments at the patient chart level by clicking on the Add New appointment button, search for a patient and open the appointments form. \
User will then attach the appointment to a service and save the appointments. There are multiple sections in the form that are must-fill before the form can be saved.

This action would then add the patient to the _Clinical appointments dashboard_ under the _booked appointments_ tab for the given day.

![alt_text](https://drive.google.com/uc?export=view&id=1S6zuvVG9TbwkKJos8EUl2zmMC7KUTcvs "image_tooltip")

**_Edit appointments_**

A user will be able to edit appointments by using the actions functions against each patient on the lists which will open the appointments form in edit mode to allow users to update the appointment details.

# **_Sync appointments_**

The module will support syncing of related appointments e.g. a case where a pair of a mother and baby need to be given the same appointment.

# **_Cancel appointments_**

A user will be able to cancel an appointment in a scenario where a patient has communicated that they would not be attending their scheduled appointment. The cancel function will be available against each appointment and this action would move the patient into the _Canceled appointments_ patient list.

# rest documentation

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
