# Mawjood

Mawjood is an office-managed lost item recovery platform built with Spring Boot 3, Thymeleaf, Spring Data JPA, MySQL, Java 17, and Maven.

## Operating Model

- Normal users create accounts, report lost items, choose the location where the item was lost, and track official office updates.
- Normal users cannot report found items, see found inventory, create claims, or message other users.
- Lost & Found Office users have a separate dashboard for their location only.
- Office users add found items, manage private inventory, review possible matches, update report status, and send case updates to the reporting user through the system.
- Admin users can review platform accounts, locations, and office ownership.

## Main Roles

- `USER`
- `OFFICE`
- `ADMIN`

## Database

Run `sql/mawjood.sql` in MySQL, or let Spring Boot create/update the schema with `spring.jpa.hibernate.ddl-auto=update`.

Main tables:

- `users`
- `locations`
- `offices`
- `lost_reports`
- `found_items`
- `case_matches`
- `case_updates`

Default database name:

```text
mawjood_db
```

## Default Accounts

```text
Admin:
Email: admin@mawjood.com
Password: admin123

Red Sea Mall Office:
Email: redsea.office@mawjood.com
Password: office123

Airport Terminal 1 Office:
Email: airport.office@mawjood.com
Password: office123
```

## Run

Check `src/main/resources/application.properties` for your MySQL username and password, then run:

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8081
```

## Test Flow

1. Register a normal user.
2. Log in as that user.
3. Submit a lost report from `/reports/new`.
4. Log out.
5. Log in as an office account for the selected location.
6. Open `/office`, add a found item, open the lost report, create a possible match, and update the case status.
7. Log back in as the normal user and confirm the report timeline shows office updates.
