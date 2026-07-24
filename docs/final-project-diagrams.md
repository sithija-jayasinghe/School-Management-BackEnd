# Final Project Diagrams

These diagrams are based on the current Spring Boot backend in `Schoool-Management-Backend` and the paired Angular frontend in `../Schoool-Management-Frontend`.

The project is a role-based school management system for administrators, teachers, and parents. It uses Angular, Angular Material, Spring Boot, Spring Security with JWT, Spring Data JPA, MySQL, local document storage, OpenAPI/Swagger, MapStruct, and PDFBox for report-card generation. No payment or fee module is included.

## 1. Overall Architecture Diagram

```mermaid
flowchart LR
    subgraph users ["Users"]
        adminUser["Administrator"]
        teacherUser["Teacher"]
        parentUser["Parent"]
    end

    subgraph client ["Client Layer"]
        angularApp["Angular SPA"]
        routeGuards["Auth and Role Guards"]
        authInterceptor["Bearer Token Interceptor"]
        featureApis["Feature API Services"]
    end

    subgraph api ["Spring Boot API"]
        restControllers["REST Controllers"]
        jwtFilter["JWT Authentication Filter"]
        methodSecurity["Method Security"]
        services["Service Layer"]
        mappers["MapStruct Mappers"]
    end

    subgraph data ["Data and Files"]
        repositories["Spring Data JPA Repositories"]
        mysqlDb[(MySQL sms_db)]
        documentStore[(uploads/documents)]
    end

    subgraph support ["Support Services"]
        swagger["OpenAPI and Swagger UI"]
        pdfService["Academic Report PDF Service"]
        seeders["Demo and Reference Data Seeders"]
    end

    adminUser --> angularApp
    teacherUser --> angularApp
    parentUser --> angularApp
    angularApp --> routeGuards
    routeGuards --> featureApis
    featureApis --> authInterceptor
    authInterceptor -->|"HTTP JSON, multipart, download"| jwtFilter
    jwtFilter --> restControllers
    restControllers --> methodSecurity
    methodSecurity --> services
    services --> repositories
    services --> mappers
    repositories --> mysqlDb
    services --> documentStore
    services --> pdfService
    restControllers --> swagger
    seeders --> repositories
```

## 2. Backend Package Diagram

```mermaid
flowchart TD
    root["org.edu application"]

    config["config (6 files)"]
    controller["controller (24 files)"]
    dto["dto (28 files)"]
    dtoRequest["dto.request (12 files)"]
    dtoResponse["dto.response (4 files)"]
    dtoParent["dto.parentportal (11 files)"]
    dtoTeacher["dto.teacherportal (12 files)"]
    entity["entity (24 files)"]
    exception["exception (8 files)"]
    mapper["mapper (16 files)"]
    repository["repository (23 files)"]
    security["security (8 files)"]
    service["service (26 files)"]
    serviceImpl["service.impl (27 files)"]
    util["util (10 files)"]

    root --> config
    root --> controller
    root --> dto
    dto --> dtoRequest
    dto --> dtoResponse
    dto --> dtoParent
    dto --> dtoTeacher
    root --> entity
    root --> exception
    root --> mapper
    root --> repository
    root --> security
    root --> service
    service --> serviceImpl
    root --> util

    controller -->|"accepts and returns"| dto
    controller -->|"delegates to"| service
    security -->|"authenticates before"| controller
    serviceImpl -->|"implements"| service
    serviceImpl -->|"uses"| repository
    serviceImpl -->|"maps through"| mapper
    serviceImpl -->|"throws"| exception
    repository -->|"persists"| entity
    mapper -->|"converts"| entity
    entity -->|"uses enums"| util
    config -->|"seeds and configures"| entity
```

## 3. Component Diagram

```mermaid
flowchart LR
    subgraph frontend ["Angular Frontend Components"]
        login["Login Feature"]
        shell["Application Shell"]
        adminFeatures["Admin Features: dashboard, users, students, parents, staff, classes, subjects, calendar, timetable, reports"]
        teacherFeatures["Teacher Portal and Teacher Features: attendance, marks, documents, leave, reports"]
        parentFeatures["Parent Portal: students, timetable, attendance, results, documents, reports, leave"]
        sharedUi["Shared UI Components"]
        authState["AuthService and Local Storage"]
    end

    subgraph backend ["Spring Boot Backend Components"]
        authComponent["Auth Component"]
        adminComponent["Admin Management Component"]
        academicComponent["Academic Calendar and Class Component"]
        teachingComponent["Teaching and Timetable Component"]
        attendanceComponent["Attendance Component"]
        resultComponent["Exam, Marks, Reports Component"]
        documentComponent["Document Component"]
        noticeComponent["Notice Component"]
        portalComponent["Parent and Teacher Portal Component"]
        auditComponent["Audit and Settings Component"]
    end

    subgraph persistence ["Persistence Components"]
        jpa["JPA Repositories"]
        db[(MySQL Database)]
        files[(Document File System)]
    end

    login --> authState
    shell --> sharedUi
    adminFeatures --> authState
    teacherFeatures --> authState
    parentFeatures --> authState

    authState --> authComponent
    adminFeatures --> adminComponent
    adminFeatures --> academicComponent
    adminFeatures --> teachingComponent
    teacherFeatures --> attendanceComponent
    teacherFeatures --> resultComponent
    teacherFeatures --> documentComponent
    teacherFeatures --> portalComponent
    parentFeatures --> portalComponent
    parentFeatures --> noticeComponent

    authComponent --> jpa
    adminComponent --> jpa
    academicComponent --> jpa
    teachingComponent --> jpa
    attendanceComponent --> jpa
    resultComponent --> jpa
    noticeComponent --> jpa
    portalComponent --> jpa
    auditComponent --> jpa
    documentComponent --> jpa
    documentComponent --> files
    jpa --> db
```

## 4. Deployment Diagram

```mermaid
flowchart LR
    subgraph browserNode ["User Browser"]
        browser["Chrome or Web Browser"]
        localStorage["Local Storage: access token and user"]
    end

    subgraph frontendNode ["Frontend Dev or Web Server"]
        angularBuild["Angular App"]
        assets["Static Assets"]
    end

    subgraph backendNode ["Application Server"]
        springBoot["Spring Boot 3.3 API"]
        embeddedTomcat["Embedded Tomcat on port 8080"]
        swaggerUi["Swagger UI"]
        uploadFolder["uploads/documents"]
    end

    subgraph databaseNode ["Database Server"]
        mysql["MySQL sms_db"]
    end

    subgraph testNode ["Automated Test Runtime"]
        h2["H2 In-Memory DB"]
        mavenTests["Maven Test Suite"]
    end

    browser -->|"loads app"| angularBuild
    angularBuild --> assets
    browser --> localStorage
    browser -->|"HTTP API: http://localhost:8080/api"| embeddedTomcat
    embeddedTomcat --> springBoot
    springBoot -->|"JDBC"| mysql
    springBoot -->|"read/write files"| uploadFolder
    browser -->|"API docs"| swaggerUi
    swaggerUi --> embeddedTomcat
    mavenTests --> springBoot
    springBoot -.->|"test profile"| h2
```

## 5. Class Diagram

```mermaid
classDiagram
    class User {
        Long id
        String name
        String email
        String password
        Role role
        boolean active
    }

    class Student {
        Long id
        String name
        LocalDate dateOfBirth
        boolean active
        String phoneNumber
    }

    class Parent {
        Long id
        String name
        String phoneNumber
        String address
        String occupation
        boolean active
    }

    class Staff {
        Long id
        String staffId
        String name
        boolean active
        String phoneNumber
        String designation
    }

    class ParentStudent {
        Long id
        String relationshipType
        boolean primaryContact
        boolean emergencyContact
    }

    class Grade {
        Long id
        String name
        Integer level
        boolean active
    }

    class SchoolClass {
        Long id
        String name
        String section
        boolean active
    }

    class Subject {
        Long id
        String code
        String name
        String description
    }

    class AcademicYear {
        Long id
        String name
        LocalDate startDate
        LocalDate endDate
        boolean current
        boolean active
    }

    class AcademicTerm {
        Long id
        String name
        LocalDate startDate
        LocalDate endDate
        boolean current
        boolean active
    }

    class StudentEnrollment {
        Long id
        LocalDate startDate
        LocalDate endDate
        EnrollmentStatus status
    }

    class TeachingAssignment {
        Long id
        boolean active
    }

    class Timetable {
        Long id
        DayOfWeek dayOfWeek
        LocalTime startTime
        LocalTime endTime
        String roomNumber
    }

    class Attendance {
        Long id
        LocalDate attendanceDate
        AttendanceStatus status
        String remarks
    }

    class LeaveRequest {
        Long id
        LocalDate startDate
        LocalDate endDate
        LeaveRequestStatus status
        boolean attendanceApplied
    }

    class Exam {
        Long id
        String name
        ExamType type
        LocalDate examDate
        BigDecimal maxMarks
        BigDecimal passMarks
    }

    class StudentMark {
        Long id
        BigDecimal marksObtained
        BigDecimal percentage
        String grade
        boolean passed
    }

    class AcademicReport {
        Long id
        AcademicReportStatus status
        BigDecimal overallPercentage
        String overallGrade
        BigDecimal attendancePercentage
    }

    class AcademicReportSubject {
        Long id
        Long subjectId
        String subjectCode
        String subjectName
        BigDecimal percentage
        String grade
        boolean passed
    }

    class Document {
        Long id
        DocumentType documentType
        String title
        String storedFileName
        boolean visibleToParent
        boolean active
    }

    class Notice {
        Long id
        String title
        NoticeAudience audience
        boolean published
        boolean active
    }

    User "1" --> "0..1" Student : profile
    User "1" --> "0..1" Parent : profile
    User "1" --> "0..1" Staff : profile
    Grade "1" --> "0..*" SchoolClass : groups
    Staff "1" --> "0..*" SchoolClass : classTeacher
    SchoolClass "1" --> "0..*" Student : currentClass
    Parent "1" --> "0..*" ParentStudent : links
    Student "1" --> "0..*" ParentStudent : links
    SchoolClass "0..*" --> "0..*" Subject : subjects
    Subject "0..*" --> "0..*" Grade : gradeSubjects
    AcademicYear "1" --> "0..*" AcademicTerm : terms
    Student "1" --> "0..*" StudentEnrollment : enrollments
    AcademicYear "1" --> "0..*" StudentEnrollment : academicYear
    SchoolClass "1" --> "0..*" StudentEnrollment : enrolledClass
    Staff "1" --> "0..*" TeachingAssignment : teaches
    SchoolClass "1" --> "0..*" TeachingAssignment : assignedClass
    Subject "1" --> "0..*" TeachingAssignment : subject
    AcademicYear "1" --> "0..*" TeachingAssignment : academicYear
    SchoolClass "1" --> "0..*" Timetable : schedule
    Subject "1" --> "0..*" Timetable : scheduledSubject
    Staff "1" --> "0..*" Timetable : scheduledTeacher
    Student "1" --> "0..*" Attendance : attendance
    SchoolClass "1" --> "0..*" Attendance : attendanceClass
    Subject "0..1" --> "0..*" Attendance : attendanceSubject
    Timetable "0..1" --> "0..*" Attendance : period
    Staff "0..1" --> "0..*" Attendance : markedBy
    Parent "1" --> "0..*" LeaveRequest : creates
    Student "1" --> "0..*" LeaveRequest : requestedFor
    Staff "0..1" --> "0..*" LeaveRequest : reviewedBy
    AcademicYear "1" --> "0..*" Exam : exams
    AcademicTerm "1" --> "0..*" Exam : termExams
    SchoolClass "1" --> "0..*" Exam : classExams
    Subject "1" --> "0..*" Exam : subjectExams
    Exam "1" --> "0..*" StudentMark : marks
    Student "1" --> "0..*" StudentMark : results
    Staff "0..1" --> "0..*" StudentMark : enteredBy
    Student "1" --> "0..*" AcademicReport : reports
    AcademicTerm "1" --> "0..*" AcademicReport : termReports
    AcademicReport "1" --> "1..*" AcademicReportSubject : subjectSnapshots
    Student "1" --> "0..*" Document : documents
    User "1" --> "0..*" Document : uploadedBy
    SchoolClass "0..1" --> "0..*" Notice : targetClass
```

## 6. ER Diagram

```mermaid
erDiagram
    USERS ||--o| STUDENTS : has_student_profile
    USERS ||--o| PARENTS : has_parent_profile
    USERS ||--o| STAFF : has_staff_profile
    USERS ||--o{ DOCUMENTS : uploads
    USERS ||--o{ AUDIT_LOGS : acts
    USERS ||--o{ ACADEMIC_REPORTS : generates
    GRADES ||--o{ SCHOOL_CLASSES : contains
    STAFF ||--o{ SCHOOL_CLASSES : owns
    SCHOOL_CLASSES ||--o{ STUDENTS : has
    SCHOOL_CLASSES }o--o{ SUBJECTS : offers
    SUBJECTS }o--o{ GRADES : assigned_to

    PARENTS ||--o{ PARENT_STUDENTS : links
    STUDENTS ||--o{ PARENT_STUDENTS : linked_to

    ACADEMIC_YEARS ||--o{ ACADEMIC_TERMS : contains
    ACADEMIC_YEARS ||--o{ STUDENT_ENROLLMENTS : tracks
    STUDENTS ||--o{ STUDENT_ENROLLMENTS : has
    SCHOOL_CLASSES ||--o{ STUDENT_ENROLLMENTS : enrolled_in

    STAFF ||--o{ TEACHING_ASSIGNMENTS : teaches
    SCHOOL_CLASSES ||--o{ TEACHING_ASSIGNMENTS : assigned
    SUBJECTS ||--o{ TEACHING_ASSIGNMENTS : assigned
    ACADEMIC_YEARS ||--o{ TEACHING_ASSIGNMENTS : scoped

    SCHOOL_CLASSES ||--o{ TIMETABLES : schedules
    SUBJECTS ||--o{ TIMETABLES : schedules
    STAFF ||--o{ TIMETABLES : teaches

    STUDENTS ||--o{ ATTENDANCE : records
    SCHOOL_CLASSES ||--o{ ATTENDANCE : records
    SUBJECTS ||--o{ ATTENDANCE : optional_subject
    TIMETABLES ||--o{ ATTENDANCE : optional_period
    STAFF ||--o{ ATTENDANCE : marks

    PARENTS ||--o{ LEAVE_REQUESTS : creates
    STUDENTS ||--o{ LEAVE_REQUESTS : requests
    STAFF ||--o{ LEAVE_REQUESTS : reviews

    ACADEMIC_YEARS ||--o{ EXAMS : has
    ACADEMIC_TERMS ||--o{ EXAMS : has
    SCHOOL_CLASSES ||--o{ EXAMS : assigned
    SUBJECTS ||--o{ EXAMS : tested
    EXAMS ||--o{ STUDENT_MARKS : has
    STUDENTS ||--o{ STUDENT_MARKS : earns
    STAFF ||--o{ STUDENT_MARKS : enters

    STUDENTS ||--o{ ACADEMIC_REPORTS : receives
    ACADEMIC_TERMS ||--o{ ACADEMIC_REPORTS : covers
    SCHOOL_CLASSES ||--o{ ACADEMIC_REPORTS : class_snapshot
    ACADEMIC_REPORTS ||--|{ ACADEMIC_REPORT_SUBJECTS : contains

    STUDENTS ||--o{ DOCUMENTS : owns
    SCHOOL_CLASSES ||--o{ NOTICES : target

    USERS {
        long id PK
        string name
        string email UK
        string password
        string role
        bool active
    }
    STUDENTS {
        long id PK
        long user_id FK
        long class_id FK
        string name
        date date_of_birth
        bool active
    }
    PARENTS {
        long id PK
        long user_id FK
        string name
        string phone_number
        bool active
    }
    STAFF {
        long id PK
        long user_id FK
        string staff_id UK
        string name
        string designation
        bool active
    }
    PARENT_STUDENTS {
        long id PK
        long parent_id FK
        long student_id FK
        string relationship_type
        bool primary_contact
        bool emergency_contact
    }
    GRADES {
        long id PK
        string name UK
        int level UK
        bool active
    }
    SCHOOL_CLASSES {
        long id PK
        long grade_id FK
        long class_teacher_id FK
        string name
        string section
        bool active
    }
    SUBJECTS {
        long id PK
        string code
        string name
        string description
    }
    ACADEMIC_YEARS {
        long id PK
        string name UK
        date start_date
        date end_date
        bool current
        bool active
    }
    ACADEMIC_TERMS {
        long id PK
        long academic_year_id FK
        string name
        date start_date
        date end_date
        bool current
        bool active
    }
    STUDENT_ENROLLMENTS {
        long id PK
        long student_id FK
        long academic_year_id FK
        long class_id FK
        string status
    }
    TEACHING_ASSIGNMENTS {
        long id PK
        long staff_id FK
        long class_id FK
        long subject_id FK
        long academic_year_id FK
        bool active
    }
    TIMETABLES {
        long id PK
        long class_id FK
        long subject_id FK
        long staff_id FK
        string day_of_week
        time start_time
        time end_time
    }
    ATTENDANCE {
        long id PK
        long student_id FK
        long class_id FK
        long subject_id FK
        long timetable_id FK
        long marked_by_staff_id FK
        date attendance_date
        string status
    }
    LEAVE_REQUESTS {
        long id PK
        long parent_id FK
        long student_id FK
        long reviewed_by_staff_id FK
        date start_date
        date end_date
        string status
    }
    EXAMS {
        long id PK
        long academic_year_id FK
        long academic_term_id FK
        long class_id FK
        long subject_id FK
        string name
        string type
    }
    STUDENT_MARKS {
        long id PK
        long exam_id FK
        long student_id FK
        long entered_by_staff_id FK
        decimal marks_obtained
        decimal percentage
        string grade
    }
    ACADEMIC_REPORTS {
        long id PK
        long student_id FK
        long academic_term_id FK
        long class_id FK
        string status
        decimal overall_percentage
    }
    ACADEMIC_REPORT_SUBJECTS {
        long id PK
        long academic_report_id FK
        long subject_id
        string subject_code
        string subject_name
        decimal percentage
    }
    DOCUMENTS {
        long id PK
        long student_id FK
        long uploaded_by_user_id FK
        string document_type
        string stored_file_name
        bool visible_to_parent
        bool active
    }
    NOTICES {
        long id PK
        long class_id FK
        string title
        string audience
        bool published
        bool active
    }
    AUDIT_LOGS {
        long id PK
        long actor_user_id FK
        string action
        string entity_type
        long entity_id
    }
    BLACKLISTED_TOKENS {
        long id PK
        string token UK
        datetime expires_at
    }
```

## 7. Sequence Diagrams

### 7.1 Login and JWT Session

```mermaid
sequenceDiagram
    title Login and JWT Session
    participant User
    participant AngularApp
    participant AuthService
    participant AuthController
    participant AuthServiceImpl
    participant UserRepository
    participant PasswordEncoder
    participant JwtService

    User->>AngularApp: Submit email and password
    AngularApp->>AuthService: login(request)
    AuthService->>AuthController: POST /api/auth/tokens
    AuthController->>AuthServiceImpl: login(request)
    AuthServiceImpl->>UserRepository: findByEmail(email)
    UserRepository-->>AuthServiceImpl: User
    AuthServiceImpl->>PasswordEncoder: matches(raw, hash)
    PasswordEncoder-->>AuthServiceImpl: valid
    AuthServiceImpl->>JwtService: generateToken(user)
    JwtService-->>AuthServiceImpl: JWT
    AuthServiceImpl-->>AuthController: AuthTokenResponse
    AuthController-->>AuthService: 200 token and user
    AuthService-->>AngularApp: Store local session
    AngularApp-->>User: Redirect by role
```

### 7.2 Teacher Bulk Attendance Marking

```mermaid
sequenceDiagram
    title Teacher Bulk Attendance Marking
    participant Teacher
    participant AngularTeacherPortal
    participant TeacherPortalController
    participant TeacherPortalService
    participant AttendanceService
    participant Repositories
    participant MySQL

    Teacher->>AngularTeacherPortal: Mark class attendance
    AngularTeacherPortal->>TeacherPortalController: POST /teacher-portal/classes/{classId}/attendance
    TeacherPortalController->>TeacherPortalService: markClassAttendance(userId, classId, request)
    TeacherPortalService->>Repositories: Load active teacher by user id
    Repositories-->>TeacherPortalService: Staff
    TeacherPortalService->>Repositories: Validate class or timetable access
    Repositories-->>TeacherPortalService: Access granted
    TeacherPortalService->>AttendanceService: markClassAttendance(bulkRequest)
    AttendanceService->>Repositories: Load class, timetable, subject, staff
    Repositories-->>AttendanceService: Related entities
    AttendanceService->>Repositories: Validate each active student and duplicates
    Repositories-->>AttendanceService: Students and no duplicate records
    AttendanceService->>MySQL: saveAll attendance records
    MySQL-->>AttendanceService: Persisted records
    AttendanceService-->>TeacherPortalService: AttendanceDTO list
    TeacherPortalService-->>TeacherPortalController: AttendanceDTO list
    TeacherPortalController-->>AngularTeacherPortal: 200 attendance records
    AngularTeacherPortal-->>Teacher: Show saved attendance
```

### 7.3 Parent Leave Request and Teacher Review

```mermaid
sequenceDiagram
    title Parent Leave Request and Teacher Review
    participant Parent
    participant ParentPortal
    participant ParentPortalController
    participant LeaveRequestService
    participant TeacherPortalController
    participant TeacherPortalService
    participant AttendanceService
    participant MySQL

    Parent->>ParentPortal: Submit leave request
    ParentPortal->>ParentPortalController: POST /parent-portal/leave-requests
    ParentPortalController->>LeaveRequestService: createParentLeaveRequest(userId, dto)
    LeaveRequestService->>MySQL: Validate parent, student link, overlap
    MySQL-->>LeaveRequestService: Valid request data
    LeaveRequestService->>MySQL: Save PENDING leave request
    MySQL-->>LeaveRequestService: LeaveRequest
    LeaveRequestService-->>ParentPortalController: LeaveRequestDTO
    ParentPortalController-->>ParentPortal: 200 pending request
    ParentPortal-->>Parent: Show request status
    TeacherPortalController->>TeacherPortalService: approveLeaveRequest(userId, requestId)
    TeacherPortalService->>LeaveRequestService: approveTeacherLeaveRequest(userId, requestId)
    LeaveRequestService->>MySQL: Validate teacher access and pending status
    MySQL-->>LeaveRequestService: Leave request
    LeaveRequestService->>MySQL: Save APPROVED review
    MySQL-->>LeaveRequestService: Approved request
    TeacherPortalController->>TeacherPortalService: applyLeaveToAttendance(userId, requestId)
    TeacherPortalService->>LeaveRequestService: applyApprovedLeaveToAttendance(userId, requestId)
    LeaveRequestService->>AttendanceService: createAttendance(EXCUSED for each date)
    AttendanceService->>MySQL: Save attendance records
    MySQL-->>AttendanceService: Saved attendance
    LeaveRequestService-->>TeacherPortalController: attendanceApplied true
```

### 7.4 Academic Report Generation and PDF Download

```mermaid
sequenceDiagram
    title Academic Report Generation and PDF Download
    participant Teacher
    participant AngularReports
    participant AcademicReportController
    participant AcademicReportService
    participant StudentMarkRepository
    participant AttendanceService
    participant ReportRepository
    participant PdfService

    Teacher->>AngularReports: Generate report
    AngularReports->>AcademicReportController: POST /api/academic-reports
    AcademicReportController->>AcademicReportService: generateReport(userId, request)
    AcademicReportService->>ReportRepository: Validate no existing report
    ReportRepository-->>AcademicReportService: No duplicate
    AcademicReportService->>StudentMarkRepository: find marks by student and term
    StudentMarkRepository-->>AcademicReportService: Marks grouped by subject
    AcademicReportService->>AttendanceService: getStudentAttendanceSummary(date range)
    AttendanceService-->>AcademicReportService: Attendance counts and percentage
    AcademicReportService->>ReportRepository: Save draft report snapshot
    ReportRepository-->>AcademicReportService: AcademicReportDTO
    AcademicReportService-->>AcademicReportController: Draft report
    AcademicReportController-->>AngularReports: 200 draft report
    Teacher->>AngularReports: Download PDF
    AngularReports->>AcademicReportController: GET /api/academic-reports/{id}/pdf
    AcademicReportController->>AcademicReportService: downloadReportCard(userId, reportId)
    AcademicReportService->>PdfService: generate(report)
    PdfService-->>AcademicReportService: DocumentFileResponse
    AcademicReportService-->>AcademicReportController: PDF file response
    AcademicReportController-->>AngularReports: application/pdf
```

### 7.5 Document Upload and Parent Download

```mermaid
sequenceDiagram
    title Document Upload and Parent Download
    participant Teacher
    participant TeacherPortal
    participant TeacherPortalController
    participant TeacherPortalService
    participant DocumentService
    participant StorageService
    participant DocumentRepository
    participant Parent
    participant ParentPortal
    participant ParentPortalController
    participant ParentPortalService

    Teacher->>TeacherPortal: Upload student document
    TeacherPortal->>TeacherPortalController: POST /teacher-portal/students/{studentId}/documents
    TeacherPortalController->>TeacherPortalService: uploadStudentDocument(userId, studentId, request, file)
    TeacherPortalService->>TeacherPortalService: Validate teacher can access student
    TeacherPortalService->>DocumentService: uploadDocument(userId, request, file)
    DocumentService->>DocumentService: Validate teacher document access
    DocumentService->>StorageService: store(file)
    StorageService-->>DocumentService: StoredDocumentFile
    DocumentService->>DocumentRepository: Save document metadata
    DocumentRepository-->>DocumentService: DocumentDTO
    DocumentService-->>TeacherPortalService: DocumentDTO
    TeacherPortalService-->>TeacherPortalController: TeacherPortalDocumentDTO
    TeacherPortalController-->>TeacherPortal: 200 document saved
    Parent->>ParentPortal: Download visible document
    ParentPortal->>ParentPortalController: GET /parent-portal/students/{studentId}/documents/{documentId}/download
    ParentPortalController->>ParentPortalService: downloadStudentDocument(userId, studentId, documentId)
    ParentPortalService->>ParentPortalService: Validate parent-student link
    ParentPortalService->>DocumentService: downloadVisibleDocument(studentId, documentId)
    DocumentService->>DocumentRepository: Find active visible document
    DocumentRepository-->>DocumentService: Document
    DocumentService->>StorageService: loadAsResource(storedFileName)
    StorageService-->>DocumentService: File resource
    DocumentService-->>ParentPortalService: DocumentFileResponse
    ParentPortalService-->>ParentPortalController: DocumentFileResponse
    ParentPortalController-->>ParentPortal: File download
```

## 8. Activity Diagrams

### 8.1 Login and Role-Based Navigation

```mermaid
flowchart TD
    start([Start])
    enterCredentials["Enter email and password"]
    submit["Submit login form"]
    findUser["Find user by email"]
    validCredentials{"Password valid and user active?"}
    issueToken["Issue JWT token"]
    storeSession["Store token and user in local storage"]
    roleDecision{"User role"}
    adminHome["Open admin dashboard"]
    teacherHome["Open teacher portal"]
    parentHome["Open parent portal"]
    showError["Show invalid credentials error"]
    finish([End])

    start --> enterCredentials --> submit --> findUser --> validCredentials
    validCredentials -->|"No"| showError --> finish
    validCredentials -->|"Yes"| issueToken --> storeSession --> roleDecision
    roleDecision -->|"ADMIN"| adminHome --> finish
    roleDecision -->|"TEACHER"| teacherHome --> finish
    roleDecision -->|"PARENT"| parentHome --> finish
```

### 8.2 Bulk Attendance Marking

```mermaid
flowchart TD
    start([Start])
    openClass["Open class attendance page"]
    chooseDate["Choose date and optional timetable period"]
    loadStudents["Load active students in class"]
    enterStatuses["Enter attendance status for each student"]
    submit["Submit bulk attendance"]
    validateTeacher["Validate teacher has class or timetable access"]
    validateStudents["Validate students belong to class"]
    duplicateDecision{"Duplicate student/date/period?"}
    saveRecords["Save attendance records"]
    showSuccess["Show saved attendance"]
    showDuplicateError["Show duplicate attendance error"]
    finish([End])

    start --> openClass --> chooseDate --> loadStudents --> enterStatuses --> submit
    submit --> validateTeacher --> validateStudents --> duplicateDecision
    duplicateDecision -->|"Yes"| showDuplicateError --> finish
    duplicateDecision -->|"No"| saveRecords --> showSuccess --> finish
```

### 8.3 Leave Request Review and Attendance Application

```mermaid
flowchart TD
    start([Start])
    parentCreates["Parent creates leave request"]
    validateLink["Validate parent-student link"]
    overlapCheck{"Overlapping request exists?"}
    savePending["Save PENDING leave request"]
    teacherReviews["Teacher reviews request"]
    accessCheck["Validate teacher class access"]
    decision{"Approve request?"}
    reject["Save REJECTED status"]
    approve["Save APPROVED status"]
    applyDecision{"Apply to attendance?"}
    createExcused["Create EXCUSED attendance for each leave date"]
    markApplied["Mark leave request attendanceApplied"]
    finish([End])

    start --> parentCreates --> validateLink --> overlapCheck
    overlapCheck -->|"Yes"| finish
    overlapCheck -->|"No"| savePending --> teacherReviews --> accessCheck --> decision
    decision -->|"No"| reject --> finish
    decision -->|"Yes"| approve --> applyDecision
    applyDecision -->|"No"| finish
    applyDecision -->|"Yes"| createExcused --> markApplied --> finish
```

### 8.4 Academic Report Generation and Publishing

```mermaid
flowchart TD
    start([Start])
    selectStudent["Select student and academic term"]
    readiness["Check report readiness"]
    readyDecision{"Has class, enrollment, marks, attendance, and no existing report?"}
    showMissing["Show missing readiness items"]
    generate["Generate draft report"]
    groupMarks["Group marks by subject"]
    calculateResults["Calculate subject and overall grades"]
    attendanceSummary["Calculate attendance summary"]
    saveDraft["Save DRAFT report snapshot"]
    reviewRemarks["Review or edit remarks"]
    publishDecision{"Publish report?"}
    publish["Set PUBLISHED status and publishedBy"]
    download["Download PDF report card"]
    finish([End])

    start --> selectStudent --> readiness --> readyDecision
    readyDecision -->|"No"| showMissing --> finish
    readyDecision -->|"Yes"| generate --> groupMarks --> calculateResults --> attendanceSummary --> saveDraft
    saveDraft --> reviewRemarks --> publishDecision
    publishDecision -->|"No"| finish
    publishDecision -->|"Yes"| publish --> download --> finish
```

### 8.5 Parent Portal Student Data Access

```mermaid
flowchart TD
    start([Start])
    parentOpens["Parent opens portal page"]
    resolveParent["Resolve active parent from JWT user"]
    chooseStudent["Parent selects linked student"]
    authorizeLink{"Active parent-student link exists?"}
    deny["Return not found for unauthorized student"]
    chooseData{"Requested data type"}
    timetable["Load class timetable"]
    attendance["Load attendance date range"]
    results["Load student marks"]
    documents["Load active visible documents"]
    reports["Load published academic reports"]
    notices["Load notices for linked student classes"]
    render["Render portal view"]
    finish([End])

    start --> parentOpens --> resolveParent --> chooseStudent --> authorizeLink
    authorizeLink -->|"No"| deny --> finish
    authorizeLink -->|"Yes"| chooseData
    chooseData -->|"Timetable"| timetable --> render
    chooseData -->|"Attendance"| attendance --> render
    chooseData -->|"Results"| results --> render
    chooseData -->|"Documents"| documents --> render
    chooseData -->|"Reports"| reports --> render
    chooseData -->|"Notices"| notices --> render
    render --> finish
```

## 9. Use Case Diagram

Mermaid does not provide a dedicated UML use-case diagram type, so this diagram uses a flowchart layout with actors on the left and system use cases grouped inside the School Management System boundary.

```mermaid
flowchart LR
    subgraph actors ["Actors"]
        guest["Guest User"]
        admin["Administrator"]
        teacher["Teacher"]
        parent["Parent"]
    end

    subgraph system ["School Management System"]
        login(["Login"])
        logout(["Logout"])
        viewDashboard(["View Dashboard"])

        manageUsers(["Manage Users"])
        manageStudents(["Manage Students"])
        manageParents(["Manage Parents"])
        manageStaff(["Manage Staff"])
        manageGrades(["Manage Grades"])
        manageClasses(["Manage Classes"])
        manageSubjects(["Manage Subjects"])
        manageAcademicCalendar(["Manage Academic Years and Terms"])
        manageTeachingAssignments(["Manage Teaching Assignments"])
        manageTimetable(["Manage Timetable"])
        manageSettings(["Manage System Settings"])
        viewAuditLogs(["View Audit Logs"])

        viewTeacherDashboard(["View Teacher Dashboard"])
        viewAssignedClasses(["View Assigned Classes and Schedule"])
        markAttendance(["Mark Class Attendance"])
        viewAttendance(["View Attendance"])
        manageExams(["Manage Exams"])
        enterMarks(["Enter Student Marks"])
        manageDocuments(["Upload and Manage Student Documents"])
        reviewLeave(["Review Leave Requests"])
        generateReports(["Generate Academic Reports"])
        publishReports(["Publish Academic Reports"])

        viewParentDashboard(["View Parent Dashboard"])
        viewLinkedStudents(["View Linked Students"])
        viewStudentTimetable(["View Student Timetable"])
        viewStudentAttendance(["View Student Attendance"])
        viewResults(["View Results"])
        viewVisibleDocuments(["View Visible Documents"])
        viewPublishedReports(["View Published Reports"])
        viewNotices(["View Notices"])
        createLeave(["Create Leave Request"])
        cancelLeave(["Cancel Leave Request"])

        downloadFiles(["Download Documents and Report Cards"])
    end

    guest --> login

    admin --> login
    admin --> logout
    admin --> viewDashboard
    admin --> manageUsers
    admin --> manageStudents
    admin --> manageParents
    admin --> manageStaff
    admin --> manageGrades
    admin --> manageClasses
    admin --> manageSubjects
    admin --> manageAcademicCalendar
    admin --> manageTeachingAssignments
    admin --> manageTimetable
    admin --> manageSettings
    admin --> viewAuditLogs
    admin --> manageExams
    admin --> enterMarks
    admin --> manageDocuments
    admin --> generateReports
    admin --> publishReports
    admin --> downloadFiles

    teacher --> login
    teacher --> logout
    teacher --> viewTeacherDashboard
    teacher --> viewAssignedClasses
    teacher --> markAttendance
    teacher --> viewAttendance
    teacher --> manageExams
    teacher --> enterMarks
    teacher --> manageDocuments
    teacher --> reviewLeave
    teacher --> generateReports
    teacher --> publishReports
    teacher --> downloadFiles

    parent --> login
    parent --> logout
    parent --> viewParentDashboard
    parent --> viewLinkedStudents
    parent --> viewStudentTimetable
    parent --> viewStudentAttendance
    parent --> viewResults
    parent --> viewVisibleDocuments
    parent --> viewPublishedReports
    parent --> viewNotices
    parent --> createLeave
    parent --> cancelLeave
    parent --> downloadFiles

    login -.-> viewDashboard
    login -.-> viewTeacherDashboard
    login -.-> viewParentDashboard
```
