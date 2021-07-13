UPDATE global_property
SET property_value= 'SELECT
  pa.uuid,
  app_service.name                                                                                AS `DASHBOARD_APPOINTMENTS_SERVICE_KEY`,
  app_service_type.name                                                                           AS `DASHBOARD_APPOINTMENTS_SERVICE_TYPE_KEY`,
  DATE_FORMAT(pa.start_date_time, "%d/%m/%Y")                                                        AS `DASHBOARD_APPOINTMENTS_DATE_KEY`,
  CONCAT(DATE_FORMAT(pa.start_date_time, "%l:%i %p"), " - ", DATE_FORMAT(pa.end_date_time, "%l:%i %p")) AS `DASHBOARD_APPOINTMENTS_SLOT_KEY`,
  CONCAT(pn.given_name, " ", pn.family_name)                                                      AS `DASHBOARD_APPOINTMENTS_PROVIDER_KEY`,
  pa.status                                                                                       AS `DASHBOARD_APPOINTMENTS_STATUS_KEY`,
  pa.appointment_kind                                                                             AS `DASHBOARD_APPOINTMENTS_KIND`,
  pa.start_date_time                                                                              AS `DASHBOARD_APPOINTMENTS_START_DATE_KEY`,
  pa.end_date_time                                                                                AS `DASHBOARD_APPOINTMENTS_END_DATE_KEY`,
  pa.tele_health_video_link
FROM
  patient_appointment pa
  JOIN person p ON p.person_id = pa.patient_id AND pa.voided IS FALSE
  JOIN appointment_service app_service
    ON app_service.appointment_service_id = pa.appointment_service_id AND app_service.voided IS FALSE
  LEFT JOIN patient_appointment_provider pap on pa.patient_appointment_id = pap.patient_appointment_id AND (pap.voided=0 OR pap.voided IS NULL)
  LEFT JOIN provider prov ON prov.provider_id = pap.provider_id AND prov.retired IS FALSE
  LEFT JOIN person_name pn ON pn.person_id = prov.person_id AND pn.voided IS FALSE
  LEFT JOIN appointment_service_type app_service_type
    ON app_service_type.appointment_service_type_id = pa.appointment_service_type_id
WHERE p.uuid = ${patientUuid} AND
      pa.start_date_time >= CURDATE() AND
      (app_service_type.voided IS FALSE OR app_service_type.voided IS NULL)
ORDER BY pa.start_date_time ASC;'
WHERE property='bahmni.sqlGet.upComingAppointments';