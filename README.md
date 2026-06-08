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
- `GET /api/parent-portal/notices` - notices relevant to parents and linked student classes
- `GET /api/parent-portal/students` - linked active students
- `GET /api/parent-portal/students/{studentId}` - linked student details
- `GET /api/parent-portal/students/{studentId}/subjects` - linked student's class subjects
- `GET /api/parent-portal/students/{studentId}/timetable` - linked student's class timetable
- `GET /api/parent-portal/students/{studentId}/attendance?from=2026-01-01&to=2026-01-31` - linked student's attendance history
- `GET /api/parent-portal/students/{studentId}/results` - linked student's exam results
- `GET /api/parent-portal/students/{studentId}/documents` - parent-visible documents for a linked student
- `GET /api/parent-portal/students/{studentId}/documents/{documentId}/download` - download a parent-visible document for a linked student
- `POST /api/parent-portal/leave-requests` - submit a leave request for a linked student
- `GET /api/parent-portal/leave-requests` - list the authenticated parent's leave requests
- `POST /api/parent-portal/leave-requests/{leaveRequestId}/cancel?remarks=...` - cancel the authenticated parent's pending leave request

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
- `GET /api/student-marks/exams/{examId}/summary` - exam result summary report
- `GET /api/student-marks/students/{studentId}` - marks history for a student

## Notice Module

Notices are managed through `/api/notices` for `ADMIN` and `TEACHER` users.

Notice audiences:

- `ALL`
- `PARENTS`
- `STUDENTS`
- `TEACHERS`
- `CLASS`

Core endpoints:

- `POST /api/notices` - create a notice
- `PATCH /api/notices/{id}` - update a notice
- `DELETE /api/notices/{id}` - deactivate a notice
- `POST /api/notices/{id}/publish` - publish a notice
- `POST /api/notices/{id}/unpublish` - unpublish a notice
- `GET /api/notices` - list active notices
- `GET /api/notices/{id}` - get one notice
- `GET /api/notices/search?title=Meeting` - search notices
- `GET /api/notices/audience/{audience}` - list notices by audience
- `GET /api/notices/classes/{classId}` - list class notices

## Teacher Portal

Teacher users can access their own portal data through `/api/teacher-portal`. These endpoints are protected with the `TEACHER` role and resolve the teacher profile from the authenticated JWT user.

Core endpoints:

- `GET /api/teacher-portal/profile` - current teacher profile
- `GET /api/teacher-portal/dashboard` - teacher profile, assigned class summary, and weekly schedule
- `GET /api/teacher-portal/classes` - active classes assigned to the teacher as class teacher
- `GET /api/teacher-portal/schedule` - weekly timetable entries assigned to the teacher
- `GET /api/teacher-portal/subjects` - subjects taught by the teacher with class and session counts
- `GET /api/teacher-portal/classes/{classId}/students` - active students in a teacher-accessible class
- `GET /api/teacher-portal/exams` - active exams relevant to the teacher's timetable subjects and classes
- `GET /api/teacher-portal/classes/{classId}/attendance?date=2026-01-05` - attendance records for a teacher-accessible class on a given date
- `POST /api/teacher-portal/classes/{classId}/attendance` - bulk mark attendance using the authenticated teacher context
- `GET /api/teacher-portal/students/{studentId}/attendance` - attendance history for a student in a teacher-accessible class
- `GET /api/teacher-portal/students/{studentId}/attendance/summary?from=2026-01-01&to=2026-01-31` - attendance summary for a student in a teacher-accessible class
- `GET /api/teacher-portal/students/{studentId}/documents` - documents for a student in a teacher-accessible class
- `POST /api/teacher-portal/students/{studentId}/documents` - upload a document for a student in a teacher-accessible class
- `GET /api/teacher-portal/students/{studentId}/documents/{documentId}/download` - download a document for a student in a teacher-accessible class
- `GET /api/teacher-portal/leave-requests?status=PENDING` - leave requests for teacher-accessible classes
- `POST /api/teacher-portal/leave-requests/{leaveRequestId}/approve` - approve a leave request as the authenticated teacher
- `POST /api/teacher-portal/leave-requests/{leaveRequestId}/reject` - reject a leave request as the authenticated teacher
- `POST /api/teacher-portal/leave-requests/{leaveRequestId}/apply-attendance` - convert an approved leave request into `EXCUSED` attendance records

## Leave Request Module

Leave requests are managed through `/api/leave-requests` for `ADMIN` and `TEACHER` users in the core workflow layer. Requests are validated against active parent-student links, prevent overlapping pending or approved periods for the same student, and move through a controlled status flow.

Statuses:

- `PENDING`
- `APPROVED`
- `REJECTED`
- `CANCELLED`

Core endpoints:

- `POST /api/leave-requests` - create a leave request
- `PATCH /api/leave-requests/{id}` - update a pending leave request
- `POST /api/leave-requests/{id}/approve` - approve a pending leave request
- `POST /api/leave-requests/{id}/reject` - reject a pending leave request
- `POST /api/leave-requests/{id}/cancel?remarks=...` - cancel a pending leave request
- `GET /api/leave-requests` - list leave requests
- `GET /api/leave-requests/{id}` - get one leave request
- `GET /api/leave-requests/status/{status}` - list leave requests by status
- `GET /api/leave-requests/students/{studentId}` - list leave requests by student
- `GET /api/leave-requests/parents/{parentId}` - list leave requests by parent

## Document Module

Documents are managed through `/api/documents` for `ADMIN` and `TEACHER` users. Files are stored on the server filesystem with metadata linked to a student record. Teacher access is limited to students in classes they teach or own.

Document types:

- `REPORT_CARD`
- `MEDICAL_RECORD`
- `LEAVE_LETTER`
- `CERTIFICATE`
- `TRANSFER_LETTER`
- `STUDENT_RECORD`
- `OTHER`

Core endpoints:

- `POST /api/documents` - upload a student document with metadata and file content
- `PATCH /api/documents/{documentId}` - update document metadata
- `DELETE /api/documents/{documentId}` - deactivate a document and remove the stored file
- `GET /api/documents/{documentId}` - get one accessible document
- `GET /api/documents/students/{studentId}` - list active documents for a student
- `GET /api/documents/{documentId}/download` - download the stored document file

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
