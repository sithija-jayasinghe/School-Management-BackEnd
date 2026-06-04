# School Management Authentication Module

Production-ready authentication module for a Spring Boot based school management backend.

## Stack

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- MySQL
- JPA / Hibernate
- JUnit 5 / Mockito

## API Endpoints

- `POST /api/users` - register a user
- `POST /api/auth/tokens` - login and receive a JWT
- `GET /api/users/me` - get the current authenticated user
- `DELETE /api/auth/tokens` - logout by blacklisting the JWT until it expires

## Module Architecture

Use [docs/MODULE_ARCHITECTURE.md](docs/MODULE_ARCHITECTURE.md) when adding a new module. It explains the project layers, naming rules, build order, and includes copy-ready templates in `docs/module-template`.

## Parent Portal

Parent users can access their own portal data through `/api/parent-portal`. These endpoints are protected with the `PARENT` role and resolve the parent profile from the authenticated JWT user, so parents cannot request another parent's data by changing a URL parameter.

- `GET /api/parent-portal/profile` - current parent profile
- `GET /api/parent-portal/dashboard` - parent profile plus linked student summary
- `GET /api/parent-portal/students` - linked active students
- `GET /api/parent-portal/students/{studentId}` - linked student details
- `GET /api/parent-portal/students/{studentId}/subjects` - linked student's class subjects
- `GET /api/parent-portal/students/{studentId}/timetable` - linked student's class timetable
- `GET /api/parent-portal/students/{studentId}/attendance?from=2026-01-01&to=2026-01-31` - linked student's attendance history

## Attendance Module

Attendance is managed through `/api/attendance` for `ADMIN` and `TEACHER` users. Parent users can only view attendance for their own linked children through the Parent Portal.

Attendance statuses:

- `PRESENT`
- `ABSENT`
- `LATE`
- `EXCUSED`

Core endpoints:

- `POST /api/attendance` - mark attendance
- `POST /api/attendance/bulk` - mark attendance for multiple students in a class
- `GET /api/attendance` - list attendance records
- `GET /api/attendance/{id}` - get one attendance record
- `PATCH /api/attendance/{id}` - update attendance
- `DELETE /api/attendance/{id}` - delete attendance
- `GET /api/attendance/students/{studentId}` - student attendance history
- `GET /api/attendance/classes/{classId}?date=2026-01-05` - class attendance for a date
- `GET /api/attendance/students/{studentId}/summary?from=2026-01-01&to=2026-01-31` - student attendance summary

## Exam Module

Exams are managed through `/api/exams` for `ADMIN` and `TEACHER` users. An exam belongs to an academic year, academic term, class, and subject.

Exam types:

- `UNIT_TEST`
- `TERM_TEST`
- `FINAL_EXAM`
- `ASSIGNMENT`
- `PRACTICAL`

Core endpoints:

- `POST /api/exams` - create an exam
- `PATCH /api/exams/{id}` - update an exam
- `DELETE /api/exams/{id}` - deactivate an exam
- `POST /api/exams/{id}/activate` - reactivate an exam
- `GET /api/exams` - list active exams
- `GET /api/exams/{id}` - get one active exam
- `GET /api/exams/search?name=Term` - search active exams
- `GET /api/exams/classes/{classId}` - list exams by class
- `GET /api/exams/academic-terms/{academicTermId}` - list exams by academic term

## Student Marks Module

Marks are managed through `/api/student-marks` for `ADMIN` and `TEACHER` users. The system calculates percentage, grade, and pass/fail status automatically from the exam's max marks and pass marks.

Grade rules:

- `A` - 75% and above
- `B` - 65% to 74.99%
- `C` - 55% to 64.99%
- `S` - 40% to 54.99%
- `F` - below 40%

Core endpoints:

- `POST /api/student-marks` - enter marks for a student exam
- `PATCH /api/student-marks/{id}` - update entered marks
- `DELETE /api/student-marks/{id}` - delete entered marks
- `GET /api/student-marks/{id}` - get one mark record
- `GET /api/student-marks/exams/{examId}` - result sheet for an exam
- `GET /api/student-marks/students/{studentId}` - marks history for a student

## Run

1. Create a MySQL database or allow auto-creation with the configured JDBC URL.
2. Set environment variables if needed:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `JWT_EXPIRATION_MS`
3. Start the application:

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

Integration tests use H2 with the `test` profile.
