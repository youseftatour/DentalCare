# Recommended appointment-duration schema change

The original Access schema had no duration column in either `TblTreatments` or
`TblAppointments`. The migration stores duration on `TblTreatments` because it
describes the standard length of a treatment.

Schema change:

```sql
ALTER TABLE TblTreatments ADD COLUMN DurationMinutes INTEGER;
```

Existing treatments were initialized with 30 minutes as an explicit migration
default. These values should be reviewed by the clinic:

```sql
UPDATE TblTreatments
SET DurationMinutes = 30
WHERE DurationMinutes IS NULL;
```

Booking queries now retrieve `DurationMinutes` from `TblTreatments`; the
scheduling service uses it without changing its interval-overlap rule. This
migration was applied to the repository's `DentalCare_Nimbus2000s.accdb` file.
