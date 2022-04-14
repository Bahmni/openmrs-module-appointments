OpenMRS Module Appointments Backend
=================================
This repository acts as the back end for the **Bahmni Appointment Scheduling**.

## Packaging
```mvn clean package```

The output is the OMOD file:
```openmrs-module-appointments/omod/target/appointments-[VERSION].omod```

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



* Appointments summary dashboard.
* Creating appointments.
* Edit appointments.
* Update appointments.
* Synchronize appointments.
* Cancel appointments.


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
