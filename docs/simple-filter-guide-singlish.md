# Simple Filter Pattern Guide

Me project eke simple filters add karanna api reusable pattern ekak use karanawa.

## Idea eka

Frontend eken normal query params yawannawa.

Example:

```text
/api/students?gender=Female&classId=2
/api/staff/filter?category=TEACHER&active=true
/api/exams/filter?classId=1&type=MID_TERM
```

Backend eke controller eka params tika `Map<String, String>` widihata gannawa. E params tika entity field walata map karanne module eke `FilterDefinitions` file eken.

## Backend files

Common reusable engine:

```text
src/main/java/org/edu/filter/FilterDefinition.java
src/main/java/org/edu/filter/FilterSpecifications.java
```

Module wise definitions:

```text
src/main/java/org/edu/filter/StudentFilterDefinitions.java
src/main/java/org/edu/filter/StaffFilterDefinitions.java
src/main/java/org/edu/filter/SubjectFilterDefinitions.java
src/main/java/org/edu/filter/ExamFilterDefinitions.java
src/main/java/org/edu/filter/AcademicYearFilterDefinitions.java
src/main/java/org/edu/filter/ClassFilterDefinitions.java
src/main/java/org/edu/filter/ParentFilterDefinitions.java
src/main/java/org/edu/filter/NoticeFilterDefinitions.java
src/main/java/org/edu/filter/UserFilterDefinitions.java
```

## Simple filter add karana hati

### Text exact filter

```java
definitions.put("gender", FilterSpecifications.equalsIgnoreCase("gender"));
```

Frontend param eka:

```text
?gender=Female
```

### ID filter

```java
definitions.put("classId", FilterSpecifications.equalsLong("currentClass.id"));
```

Frontend param eka:

```text
?classId=2
```

### Boolean filter

```java
definitions.put("active", FilterSpecifications.equalsBoolean("active"));
```

Frontend param eka:

```text
?active=true
```

### Enum filter

```java
definitions.put("role", FilterSpecifications.equalsEnum("role", Role.class));
```

Frontend param eka:

```text
?role=ADMIN
```

### Date range filter

```java
definitions.put("from", FilterSpecifications.dateGreaterThanOrEqual("examDate"));
definitions.put("to", FilterSpecifications.dateLessThanOrEqual("examDate"));
```

Frontend params:

```text
?from=2026-08-01&to=2026-08-31
```

### Search keyword filter

```java
private static FilterDefinition<Student> keywordFilter() {
    return rawValue -> FilterSpecifications.<Student>likeIgnoreCase("name", rawValue)
            .or(FilterSpecifications.likeIgnoreCase("admissionNumber", rawValue));
}
```

## New module ekakata filter support denna

1. Repository ekata `JpaSpecificationExecutor<Entity>` add karanna.

```java
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
}
```

2. Controller eke list endpoint eka `Map<String, String>` ganna hadanna.

```java
public Page<StudentDTO> getAllStudents(@RequestParam Map<String, String> filters, Pageable pageable) {
    return studentService.filterStudents(filters, pageable);
}
```

3. Service method eka map accept karanna.

```java
Page<StudentDTO> filterStudents(Map<String, String> filters, Pageable pageable);
```

4. Service implementation eke common engine eka use karanna.

```java
return studentRepository.findAll(
        FilterSpecifications.build(filters, StudentFilterDefinitions.definitions()),
        pageable
).map(studentMapper::toDTO);
```

5. Module definitions file ekata allowed filters tika add karanna.

```java
definitions.put("active", FilterSpecifications.equalsBoolean("active"));
definitions.put("keyword", keywordFilter());
```

## Viva answer eka

Api filter architecture eka reusable JPA Specification based pattern ekak. Frontend query params yawannawa. Backend eka allowed params tika module-specific definition map ekakata match karala dynamic query ekak build karanawa. E nisa simple filter ekak add karanna repository query rewrite karanna one naha. UI field eka add karala backend definitions file eke one line ekak add kalama athi.
