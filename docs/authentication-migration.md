# Password authentication migration

The Access database now stores BCrypt password hashes in the shared person row:

```sql
ALTER TABLE TblPersons ADD COLUMN PasswordHash TEXT(60);
```

No default passwords were created. Existing and newly created accounts cannot
log in until an administrator provisions a password for their `PersonId`.

After compiling, run `main.PasswordHashTool` from an interactive terminal or
from Eclipse with one program argument containing the person's ID. The tool
prompts for the password without echoing it and stores only its BCrypt hash.

Patients continue to log in with `TblPatients.Identifier`; staff continue to
log in with `TblStaff.StaffId`. Both must also supply the provisioned password.
