# Recommended appointment-duration schema change

The current Access schema has no duration column in either `TblTreatments` or
`TblAppointments`. DentalCare therefore continues to use a documented 30-minute
compatibility default in `AppointmentSchedulingService`.

When the schema change is approved, duration should be stored on the treatment
because it describes the standard length of that treatment:

```sql
ALTER TABLE TblTreatments ADD COLUMN DurationMinutes INTEGER;
```

Before making the Java application require this value, populate every existing
treatment with a reviewed positive duration. A temporary value of 30 can be used
only as an explicit migration default and should be checked by the clinic:

```sql
UPDATE TblTreatments
SET DurationMinutes = 30
WHERE DurationMinutes IS NULL;
```

After migration, booking queries should retrieve `DurationMinutes` from
`TblTreatments`; the scheduling service can then use it without changing its
interval-overlap rule. This document is advisory only. No database change has
been applied.
