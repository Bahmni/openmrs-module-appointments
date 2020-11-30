UPDATE global_property
SET property_value= "SELECT
  app_service.uuid,
  app_service.name                                                                                AS `DASHBOARD_APPOINTMENTS_SERVICE_KEY`,
  app_service_type.name                                                                           AS `DASHBOARD_APPOINTMENTS_SERVICE_TYPE_KEY`,
  DATE_FORMAT(start_date_time, \"%d/%m/%Y\")                                                        AS `DASHBOARD_APPOINTMENTS_DATE_KEY`,
  CONCAT(DATE_FORMAT(start_date_time, \"%l:%i %p\"), \" - \", DATE_FORMAT(end_date_time, \"%l:%i %p\")) AS `DASHBOARD_APPOINTMENTS_SLOT_KEY`,
  CONCAT(pn.given_name, ' ', pn.family_name)                                                      AS `DASHBOARD_APPOINTMENTS_PROVIDER_KEY`,
  pa.status                                                                                       AS `DASHBOARD_APPOINTMENTS_STATUS_KEY`
FROM
  patient_appointment pa
  JOIN person p ON p.person_id = pa.patient_id AND pa.voided IS FALSE
  JOIN appointment_service app_service
    ON app_service.appointment_service_id = pa.appointment_service_id AND app_service.voided IS FALSE
  LEFT JOIN provider prov ON prov.provider_id = pa.provider_id AND prov.retired IS FALSE
  LEFT JOIN person_name pn ON pn.person_id = prov.person_id AND pn.voided IS FALSE
  LEFT JOIN appointment_service_type app_service_type
    ON app_service_type.appointment_service_type_id = pa.appointment_service_type_id
WHERE p.uuid = ${patientUuid} AND
      start_date_time >= CURDATE() AND
      (app_service_type.voided IS FALSE OR app_service_type.voided IS NULL)
ORDER BY start_date_time ASC;"
WHERE property='bahmni.sqlGet.upComingAppointments'