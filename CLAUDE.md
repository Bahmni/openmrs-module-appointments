# Durable facts for this repository

## Build

- Module version: `2.3.0-SNAPSHOT`. Platform: OpenMRS 2.8.10 (needs 2.8.0+ to start),
  webservices.rest 3.6.0, Java 17 source/target, Liquibase 4.32 at runtime.
- Build and test on JDK 17 (point `JAVA_HOME` at a local JDK 17 install) with
  `./mvnw clean verify`.
- Suite baseline: 859 tests, 0 failures, 4 skipped (api unit 328/2S, api IT 106,
  atom-feed 20, omod unit 276/1S, omod IT 129/1S). A run that reports different
  totals changed behaviour, not just code.
- Mockito 5.7.0 (inline maker is the default; `mockito-inline` must not come back).
  PowerMock is gone; static mocking uses `Mockito.mockStatic`.

## Liquibase rules

- The six `bahmni.sqlGet.*Appointments` global-property changesets are
  `runOnChange`. Their stored `md5sum` must never change between releases:
  the v8 checksum is computed from the normalized SQL **content** only, so
  editing the SQL (or the `.sql` files they were once loaded from) re-runs the
  changeset and **overwrites implementer-customized global-property values**.
- The changesets are inline `<sql><![CDATA[…]]></sql>`; the loose
  `patient*Appointments*.sql` files in `api/src/main/resources` are historical
  and no longer referenced. Do not convert back to `<sqlFile>`: on OpenMRS
  2.8.x (Liquibase 4.32) module `sqlFile` paths cannot be resolved at all —
  the changelog pre-count fails with `FileNotFoundException` before the
  changeset can even be skipped (`relativeToChangelogFile` does not help,
  tried and measured).
- Fixture rule: never point fixtures at person 1 / patient 1 (the admin's
  person). Loading them together with a patient association throws
  `WrongClassException` on Hibernate 5.6. Use a dedicated fixture patient
  with its own `person_name` and `birthdate` rows.
- The api integration tests commit between successive dataset loads: the
  2.8 `RolePrivilegeCache` daemon only sees committed rows, and the admin
  user must remain authenticated after the datasets refresh user 1.

## Privilege rules

- Since platform 2.6.11, reading a global property requires
  `Get Global Properties`. Any production code path that reads a global
  property on a user-facing request must wrap the read in
  `Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES)` /
  `removeProxyPrivilege` in `try/finally` (see
  `DefaultAppointmentStatusChangeValidator`).
- Hibernate `Example.create` over-constrains on 2.8.x (it matches on
  defaults such as `isTeleconsultationEnabled`). Search criteria must
  restrict explicitly on the fields callers populate (see
  `AppointmentDaoImpl.search`).
- On a real 2.8 host, creating a role through REST auto-grants read
  prerequisite privileges (including `Get Global Properties`); strip them
  in SQL when a test role must hold only appointment privileges.

## Host boot (Docker)

- `openmrs/openmrs-core:2.8.x` against an **existing** database needs
  `OMRS_CREATE_TABLES=false`; with the default the wizard runs the
  fresh-install schema snapshot against the populated schema and the
  startup rolls back to the `initialsetup` screen.
- openmrs-atomfeed 2.7.0-SNAPSHOT requires the legacyui module to start.
