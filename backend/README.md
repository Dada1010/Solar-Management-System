# Aditya Solar Management API

Liquibase owns database schema changes. Add a new, uniquely identified changeset under `src/main/resources/db/changelog/changes` and include it in `db.changelog-master.xml` for each schema change. Do not edit a changeset after it has been applied to a shared database.

The initial changesets create the company, branch, and employee tables. If those tables already exist, the baseline changesets are marked as applied; Hibernate then validates the existing schema at startup.