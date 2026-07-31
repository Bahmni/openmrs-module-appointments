# Implementation Plan: Dynamic Appointment Service Attribute criteria support in `/appointmentSearch`

> **Update (post-review):** The initial requirement asked specifically for a `destinationCountry` criteria. During implementation review it was decided **not** to hard-code a single attribute type. Instead, a **generic, dynamic field-name convention** was implemented so that clients can filter by **any** admin-configured `AppointmentServiceAttributeType` (e.g. "Destination Country", "Department Code", or any future attribute type) without further code changes. This document reflects the dynamic design that was implemented.

## 1. Requirement

The `/appointmentSearch` API (backed by `AppointmentSearchService` → `AppointmentSearchServiceImpl` → `AppointmentSearchDao` → `AppointmentCriteriaBuilder`) should accept a criteria in the search request payload that filters appointments by a value configured on the **Appointment Service** the appointment belongs to, via the existing admin-configurable "Appointment Service Attribute" mechanism (e.g. "Destination Country" is one such attribute type among potentially many that may be configured by an implementation).

## 2. Where the data actually lives

Attributes like "Destination Country" are **not** first-class columns anywhere. They are stored using the existing generic (EAV-style) attribute framework that already backs `AppointmentServiceDefinition`:

```
patient_appointment (appointment)
   └─ appointment_service_id  ──►  appointment_service (AppointmentServiceDefinition)
                                        └─ appointment_service_attribute (AppointmentServiceAttribute)
                                              ├─ attribute_type_id ──► appointment_service_attribute_type (AppointmentServiceAttributeType.name = e.g. "Destination Country")
                                              └─ value_reference   (the actual value, stored as text)
```

Relevant existing classes:
- `Appointment.service` → `AppointmentServiceDefinition`
- `AppointmentServiceDefinition.attributes` → `Set<AppointmentServiceAttribute>` (hibernate mapping: `AppointmentServiceDefinition.hbm.xml`, one-to-many, `inverse="true"`, filtered by `voided`)
- `AppointmentServiceAttribute.attributeType` → `AppointmentServiceAttributeType` (has `name`, `datatypeClassname`, etc.)
- `AppointmentServiceAttribute.valueReference` → raw text value (hydrated value obtained via `getValue()` if a `datatypeClassname` is configured — see `AppointmentServiceMapper.constructAttributeResponse`)

This is a **dynamic/admin-configurable attribute** mechanism — any implementation can define any number of `AppointmentServiceAttributeType` rows (e.g. "Destination Country", "Department Code", etc.) via the "Manage Appointment Service Attribute Types" admin screen. Since the set of attribute types is open-ended and instance-specific, the search support was designed generically rather than hard-coding a single attribute type name.

## 3. How the current generic search framework works

The `/appointmentSearch` request criteria is a generic `SearchCondition` tree (from `bahmni-java-utils` `search-commons`), evaluated by:

- `AppointmentSearchFields` — string constants naming searchable fields (e.g. `location.uuid`, `appointment.serviceType`)
- `AppointmentCriteriaBuilder` (extends `AbstractCriteriaBuilder<Appointment>`) — holds a `Map<String, FieldConfig>` (`fieldRegistry`) that maps each **static** field name to:
  - `joinPath` — a **single-level** JPA join from `Appointment` root (e.g. `"service"`, `"location"`), or `null` for a direct property
  - `propertyName` — the property to compare on the resolved `From` (e.g. `"uuid"`, `"startDateTime"`)
  - `FieldType` — `STRING` (supports `EQ`) or `DATE` (supports `GT`/`LT`)
- For every leaf condition, `buildLeafCriterion` resolves the join (cached in `joinCache`) and builds a single `cb.equal`/`cb.greaterThan`/`cb.lessThan` predicate against **one** property.

This static-registry approach works well for simple `entity.property` or `entity.joinedEntity.property` (1-hop) equality checks, but it has **no support** for:
1. Multi-hop joins (`Appointment → service → attributes → attributeType` is 3 hops).
2. Compound/EAV predicates — a single logical "field" that requires **two correlated conditions on the same joined row** (`attributeType.name = <X> AND valueReference = <input value>`), rather than one column equality.
3. **Dynamic** field names not known in advance (any attribute type name an admin has configured).

## 4. Design implemented

### 4.1 Field name convention (dynamic)

Rather than adding one static constant per attribute type, a **prefix-based dynamic field name convention** was introduced in `AppointmentSearchFields`:

```java
public static final String SERVICE_ATTRIBUTE_FIELD_PREFIX = "appointment.service.attribute.";

public static String serviceAttributeField(String attributeTypeName) {
    return SERVICE_ATTRIBUTE_FIELD_PREFIX + attributeTypeName;
}
```

Clients build the search field name by appending the **exact** attribute type name (as configured via the "Manage Appointment Service Attribute Types" admin screen) after the prefix, e.g.:

```
appointment.service.attribute.Destination Country
appointment.service.attribute.Department Code
```

This means **any** attribute type — present or future — is automatically searchable with **zero code changes**, as long as the client knows/discovers the attribute type name.

### 4.2 Predicate construction (`AppointmentCriteriaBuilder`)

`buildLeafCriterion` now checks whether the incoming field name starts with `SERVICE_ATTRIBUTE_FIELD_PREFIX`:
- If so, it extracts the attribute type name (the substring after the prefix) and delegates to `buildServiceAttributePredicate`, which:
  1. Resolves/caches the existing `service` join (reusing the same join used by `serviceType`, via the existing single-hop `resolveFrom`).
  2. Joins `service → attributes` and `attributes → attributeType`, with join-cache keys **namespaced per attribute type name** (e.g. `service.attributes[Destination Country]`, `service.attributes.attributeType[Destination Country]`) so that:
     - Repeated leaf conditions for the **same** attribute type reuse the same join (no duplicate joins).
     - Different attribute types used together in the same query (e.g. `Destination Country` AND `Department Code` combined) each get their **own independent join**, avoiding incorrect cross-matching between unrelated attribute rows.
  3. Builds the compound predicate: `attributeType.name = <attributeTypeName> AND attribute.valueReference = <input value> AND attribute.voided = false`.
- Otherwise, it falls through unchanged to the existing static `fieldRegistry` lookup — so all existing fields (`location`, `serviceType`, `appointmentDate`, `appointmentNumber`) are completely unaffected.
- Only the `EQ` comparator is supported (validated via `FieldType.STRING`), consistent with the free-text nature of `valueReference`.

This was implemented **locally within `AppointmentCriteriaBuilder`** (no changes to the shared `bahmni-java-utils` `search-commons` library), keeping the change contained to `openmrs-module-appointments` and avoiding any regression risk or version-coordination overhead with other consumers of that shared library (e.g. `openmrs-module-episodes`).

## 5. Detailed file-level changes (as implemented)

| File | Change |
|---|---|
| `AppointmentSearchFields.java` | Added `SERVICE_ATTRIBUTE_FIELD_PREFIX = "appointment.service.attribute."` and helper `serviceAttributeField(String attributeTypeName)`. |
| `AppointmentSearchConstants.java` | Added constants: `ATTRIBUTES = "attributes"`, `ATTRIBUTE_TYPE = "attributeType"`, `VALUE_REFERENCE = "valueReference"` (reused the existing `NAME`/`VOIDED`/`SERVICE` constants already present). |
| `AppointmentCriteriaBuilder.java` | - In `buildLeafCriterion`, detect field names starting with `SERVICE_ATTRIBUTE_FIELD_PREFIX` and delegate to a new `buildServiceAttributePredicate` method before falling back to the static `fieldRegistry`.<br>- New method `buildServiceAttributePredicate` performs the multi-hop join (`service` → `attributes` → `attributeType`) with per-attribute-type-name-namespaced join cache keys, and builds the compound `AND` predicate (attribute type name match + value match + not-voided). |
| `AppointmentSearchDaoImpl.java` | **To be verified in next step** — confirm existing `root.fetch(SERVICE, LEFT)` (used for response hydration) doesn't conflict with the new plain `join` calls used for filtering, and confirm `distinct(true)` still de-duplicates correctly with multi-attribute test data. |
| `AppointmentSearchService` / `AppointmentSearchServiceImpl` | No interface change required — criteria remain a generic `SearchCondition` tree. |
| `CriteriaValidator.java` | No change required; comparator validation already enforced via `FieldType.STRING.supports(EQ)` inside the builder. |
| `AppointmentSearchConstants.java` | **Done** — Added `DESTINATION_COUNTRY_ATTRIBUTE_TYPE_NAME = "Destination Country"` and `DESTINATION_COUNTRY = "destinationCountry"` constants. |
| `AppointmentResponseBuilder.java` | **Done** — `buildServiceMap()` now includes a `destinationCountry` field, resolved via a new `resolveDestinationCountry(service)` helper that scans `service.getActiveAttributes()` for the attribute whose `attributeType.name` equals `"Destination Country"`, returning its `valueReference` (or `null` if not set/voided). This exposes the same value used for filtering back in the `/appointmentSearch` response, e.g.:<br>`"service": { "uuid": "...", "name": "Radiology", "description": "...", "destinationCountry": "India" }`.<br>**Design decision:** a specific `destinationCountry` field (rather than a generic `attributes` map of all service attributes) was chosen for the response, to keep the response payload stable/predictable for consumers, since only this one attribute type was called out as needing to be surfaced in the response. |
| Documentation (`docs/`) | This document — describes the dynamic field name convention, supported comparator (`eq` only), and expected value format in the `/appointmentSearch` payload, and the new response field. |
| Tests | Unit tests intentionally **not added** for `AppointmentResponseBuilder`'s POJO-wiring logic (per team convention of not testing simple model/POJO getter-chains). Still **pending**: unit tests for `AppointmentCriteriaBuilder` (new dynamic predicate branch, multi-attribute-type join isolation, AND/OR combination with existing fields); DAO integration tests with test data seeding an attribute type + attribute value. |


## 6. Complications & Risks

1. **Dynamic field name relies on exact attribute type name string matching** — since the field name embeds the literal attribute type name (`appointment.service.attribute.Destination Country`), it is sensitive to:
   - Exact spelling/case (the predicate does a case-sensitive `cb.equal` on `attributeType.name`).
   - Whitespace in the name (e.g. "Destination Country" — the dot-prefix convention plus a space-containing suffix must be handled correctly by client-side URL/JSON encoding of the field name).
   - Renames of the attribute type on the admin screen will silently break any saved/bookmarked client-side search configuration using the old name.
   
   Mitigation: clients should ideally discover valid attribute type names via an existing "list appointment service attribute types" endpoint rather than hard-coding them, and documentation should clearly state the exact-match, case-sensitive nature of this field.

2. **No attribute-type-existence validation** — if a client passes an attribute type name that doesn't exist (typo, retired type, wrong case), the query will simply return **zero matches** silently rather than an explicit error, since the `INNER JOIN` on `attributeType.name = <name>` just filters out all appointments whose service doesn't have that attribute. Consider whether this is acceptable or whether a pre-validation step (checking the name exists via `AppointmentServiceAttributeTypeService` before building the query) should be added to fail fast with a clear `400 Bad Request`.

3. **Value representation / datatype hydration** — `valueReference` stores the raw persisted text. If a given attribute type is configured with a `datatypeClassname` (e.g. a coded/concept datatype) rather than plain free-text, the stored `valueReference` will not equal the human-readable value clients would search by (see how `AppointmentServiceMapper.constructAttributeResponse` hydrates via `attribute.getValue()` for display). The predicate as implemented compares raw `valueReference`, so:
   - If the attribute type is plain **free text**, direct equality works out of the box.
   - If it uses a coded/concept datatype, the API would need to convert the incoming search value to the persisted representation before building the predicate — this is **not yet implemented** and is an explicit limitation of the current design (documented so implementers know to constrain such attribute types to plain text if they want them to be searchable this way).

4. **Cardinality / voided attributes** — `AppointmentServiceAttributeType.minOccurs`/`maxOccurs` theoretically allow more than one value per type, and historical edits leave `voided=true` rows behind. The predicate filters `voided = false`; if `maxOccurs > 1` is allowed for a given type, the current implementation behaves as "ANY active value matches" (natural behavior of a collection join), which should be confirmed as the desired semantics for all attribute types used this way (not just Destination Country).

5. **Duplicate rows from collection joins** — joining the `Set<AppointmentServiceAttribute>` collection can multiply `Appointment` rows if a service has multiple attribute rows (even across different attribute types). The existing `query.select(root).distinct(true)` (in `AppointmentSearchDaoImpl`, to be verified) should still de-duplicate correctly, but this **must be verified with a test** that has a service with multiple attributes of different types to ensure no duplicate/incorrect results — this is the next pending step.

6. **Combining multiple attribute-type filters in one query (AND/OR)** — since `SearchCondition` supports arbitrary AND/OR nesting, a client could combine e.g. `appointment.service.attribute.Destination Country = 'USA'` AND `appointment.service.attribute.Department Code = 'D1'` in the same request. Because join cache keys are namespaced per attribute type name (see Section 4.2), each gets an independent join, which correctly avoids the classic EAV pitfall of accidentally requiring both conditions to match on the *same* attribute row (which would be impossible since a row has only one attribute type). This was specifically designed for and should be covered by a dedicated test.

7. **Performance** — adding two more join levels (`attributes`, `attributeType`) to the appointment search query increases query complexity, and each distinct attribute type name used in a query adds its own additional join pair. Should be validated against realistic data volumes; consider whether an index on `appointment_service_attribute(attribute_type_id, value_reference)` is warranted if this becomes a commonly used filter pattern.

8. **Backward compatibility** — the new field name prefix is a purely additive, optional criteria; requests that don't include it behave exactly as before. No changes to `AppointmentSearchService` interface signature were needed since criteria is already a generic tree. Existing static fields (`location`, `serviceType`, `appointmentDate`, `appointmentNumber`) are unaffected — verified by successful compilation of the module.

9. **Cross-module scope** — this was implemented purely within `openmrs-module-appointments`; **no changes were made to `bahmni-java-utils` search-commons**, so `openmrs-module-episodes` (the other consumer of that shared library) is completely unaffected by this change.

## 7. Implementation task list

- [x] Confirm field name convention: dynamic prefix `appointment.service.attribute.<AttributeTypeName>` (not a single hard-coded `destinationCountry` field).
- [x] Confirm implementation approach: local special-case in `AppointmentCriteriaBuilder` only (no changes to shared `bahmni-java-utils`).
- [x] Add new constants/helper to `AppointmentSearchFields` (`SERVICE_ATTRIBUTE_FIELD_PREFIX`, `serviceAttributeField(...)`).
- [x] Add new constants to `AppointmentSearchConstants` (`ATTRIBUTES`, `ATTRIBUTE_TYPE`, `VALUE_REFERENCE`).
- [x] Extend `AppointmentCriteriaBuilder`:
  - [x] Detect the dynamic field prefix in `buildLeafCriterion` and extract the attribute type name.
  - [x] Multi-hop join resolution (`service` → `attributes` → `attributeType`) with per-attribute-type-name-namespaced join cache keys.
  - [x] Compound (type-name + value + not-voided) predicate construction.
  - [x] Verified existing static fields (`location`, `serviceType`, `appointmentDate`, `appointmentNumber`) remain unaffected; module compiles cleanly.
- [ ] Verify/adjust `AppointmentSearchDaoImpl` joins (plain `join`, not `fetch`, for filter-only paths) and confirm `distinct` behavior with multi-attribute test data.
- [x] Include `destinationCountry` in the `/appointmentSearch` response payload via `AppointmentResponseBuilder` (specific field, not a generic attributes map — see Section 5).
- [ ] Add/extend unit tests for `AppointmentCriteriaBuilder` covering: single attribute-type filter, multiple different attribute-type filters combined via AND/OR (verifying independent joins), non-existent attribute type name (expect zero results, not error), regression on existing static fields.

- [ ] Add DAO/integration tests with test data covering: matching value, non-matching value, voided attribute (should be excluded), multiple attributes of different types on same service (no duplicate rows), attribute type with no matching name (empty result).
- [ ] Update API/consumer documentation for `/appointmentSearch` describing the dynamic field name convention, supported comparator (`eq`), case-sensitivity, and expected value format.
- [ ] Regression test all existing `/appointmentSearch` filters (`location`, `serviceType`, `appointmentDate`, `appointmentNumber`) to ensure no impact.
- [ ] Coordinate rollout with FE and implementation/config teams (communicate the dynamic field-name convention so any admin-configured attribute type is usable without further backend changes).
- [ ] Decide whether to add fail-fast validation for non-existent attribute type names (currently silently returns zero results).
