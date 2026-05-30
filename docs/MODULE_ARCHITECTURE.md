# Module Architecture Guide

This project uses a simple layered Spring Boot architecture. Every new business feature should be added as a module with the same shape, so a developer can copy one existing module and replace the names safely.

Good existing modules to copy:

- Simple CRUD module: `Subject`
- CRUD module with date/business fields: `AcademicYear`
- Module with relationships: `Class`, `ParentStudent`, `Timetable`

## Standard Module Flow

Request path:

```text
Client
  -> Controller
  -> Service interface
  -> Service implementation
  -> Repository
  -> Entity / Database
```

Response path:

```text
Entity
  -> Mapper
  -> DTO
  -> Controller response
```

## Folder Responsibility

| Folder | Purpose | Example |
| --- | --- | --- |
| `entity` | Database table model | `Subject.java` |
| `dto` | Data accepted from and returned to API users | `SubjectDTO.java` |
| `repository` | Database access methods | `SubjectRepository.java` |
| `mapper` | Convert Entity <-> DTO | `SubjectMapper.java` |
| `service` | Public business method contract | `SubjectService.java` |
| `service/impl` | Business logic implementation | `SubjectServiceImpl.java` |
| `controller` | REST API endpoints | `SubjectController.java` |
| `exception` | Reusable API errors | `ResourceNotFoundException.java` |

## Naming Rules

Use the same name everywhere.

Example module name: `Exam`

| Layer | File name |
| --- | --- |
| Entity | `Exam.java` |
| DTO | `ExamDTO.java` |
| Repository | `ExamRepository.java` |
| Mapper | `ExamMapper.java` |
| Service | `ExamService.java` |
| Service implementation | `ExamServiceImpl.java` |
| Controller | `ExamController.java` |
| API URL | `/api/exams` |
| DB table | `exams` |

## Step-by-Step: Add a New Module

1. Create the entity in `src/main/java/org/edu/entity`.
2. Create the DTO in `src/main/java/org/edu/dto`.
3. Create the repository in `src/main/java/org/edu/repository`.
4. Create the mapper in `src/main/java/org/edu/mapper`.
5. Create the service interface in `src/main/java/org/edu/service`.
6. Create the service implementation in `src/main/java/org/edu/service/impl`.
7. Create the controller in `src/main/java/org/edu/controller`.
8. Add any custom repository search methods.
9. Add validation annotations to DTO fields.
10. Run `mvn test`.

## Copy Template

Template files are in:

```text
docs/module-template
```

Copy each `.template` file to the matching source folder, then replace:

| Placeholder | Replace with |
| --- | --- |
| `{{Module}}` | PascalCase singular name, for example `Exam` |
| `{{module}}` | camelCase singular name, for example `exam` |
| `{{modules}}` | lowercase plural URL/table name, for example `exams` |
| `{{ModuleDTO}}` | DTO class name, for example `ExamDTO` |

## Default CRUD Endpoints

Every simple module should expose this basic API unless there is a strong reason not to:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/{{modules}}` | Create item |
| `GET` | `/api/{{modules}}` | List items with pagination |
| `GET` | `/api/{{modules}}/{id}` | Get one item |
| `PATCH` | `/api/{{modules}}/{id}` | Update item partially |
| `DELETE` | `/api/{{modules}}/{id}` | Delete item |
| `GET` | `/api/{{modules}}/search?keyword=value` | Search by name/title/code |

## Beginner Checklist

Before saying a module is complete, check this list:

- Entity has `@Entity`, `@Table`, `@Id`, and `@GeneratedValue`.
- DTO has validation like `@NotBlank`, `@NotNull`, `@Email`, or `@Size`.
- Repository extends `JpaRepository<Entity, Long>`.
- Mapper extends `BaseMapper<DTO, Entity>`.
- Service interface has create, update, delete, get all, get by id, and search methods.
- Service implementation throws `ResourceNotFoundException` when an ID is missing.
- Controller uses `@RestController`, `@RequestMapping`, and `@Valid` for request bodies.
- Method names are consistent and easy to search.
- Tests pass with `mvn test`.

## Relationship Rules

For relationships, keep DTOs simple. Use IDs instead of nesting full objects.

Example:

```java
private Long classId;
private List<Long> subjectIds;
```

In the service implementation:

1. Read related IDs from the DTO.
2. Load related entities using their repositories.
3. If any related entity is missing, throw `ResourceNotFoundException`.
4. Set the relationship on the entity.
5. Save the main entity.

## Validation Rules

Put user input validation in DTO classes.

Common annotations:

- `@NotBlank` for required text.
- `@NotNull` for required numbers, dates, IDs, and enums.
- `@Email` for email fields.
- `@Size` for text length.
- `@Past` or `@Future` for date rules.

Controller methods should use `@Valid`:

```java
public {{ModuleDTO}} create(@Valid @RequestBody {{ModuleDTO}} dto)
```

## Error Rules

Use existing reusable exceptions first:

- Missing record: `ResourceNotFoundException`
- Duplicate data: create or reuse a conflict exception
- Invalid user input: `IllegalArgumentException` or a specific validation exception

Do not return raw `RuntimeException` for normal API errors. Use a known exception so `GlobalExceptionHandler` can return a clean response.

## Recommended Module Build Order

Build in this order because each layer depends on the previous one:

```text
Entity -> DTO -> Repository -> Mapper -> Service -> ServiceImpl -> Controller -> Test
```

When stuck, compare with:

```text
Subject.java
SubjectDTO.java
SubjectRepository.java
SubjectMapper.java
SubjectService.java
SubjectServiceImpl.java
SubjectController.java
```
