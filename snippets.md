# Final Year Project Modification Snippets

Use this file as a viva/code-modification cheat sheet.

Rule: do not paste every snippet blindly. First find the closest existing module, copy the matching pattern, then replace names.

Backend root:

```txt
/Users/SithijaJayasinghe/projects/iCET/My Projects/Schoool-Management-Backend
```

Frontend root:

```txt
/Users/SithijaJayasinghe/projects/iCET/My Projects/Schoool-Management-Frontend
```

## 0. Fast Survival Commands

Find exact text, button, error, endpoint, or field:

```bash
rg "Could not save timetable entry"
rg "formControlName"
rg "staff/filter"
rg "@GetMapping"
rg "schoolStartTime"
```

Find backend module:

```bash
rg "Student" src/main/java/org/edu
rg "Timetable" src/main/java/org/edu
rg "Attendance" src/main/java/org/edu
```

Find frontend module:

```bash
rg "student" "/Users/SithijaJayasinghe/projects/iCET/My Projects/Schoool-Management-Frontend/src/app/features/students"
rg "timetable" "/Users/SithijaJayasinghe/projects/iCET/My Projects/Schoool-Management-Frontend/src/app/features/timetable"
```

Build/test after edits:

```bash
npm run build
./apache-maven-3.9.6/bin/mvn test
./apache-maven-3.9.6/bin/mvn -Dtest=StudentServiceImplTest test
```

## 1. Project Pattern

Every backend modification usually touches:

```txt
entity/<Entity>.java
dto/<Entity>DTO.java OR dto/request/<Request>.java
repository/<Entity>Repository.java
service/<Entity>Service.java
service/impl/<Entity>ServiceImpl.java
controller/<Entity>Controller.java
mapper/<Entity>Mapper.java
src/test/java/org/edu/service/impl/<Entity>ServiceImplTest.java
```

Every frontend modification usually touches:

```txt
src/app/features/<module>/models/<module>.model.ts
src/app/features/<module>/services/<module>-api.service.ts
src/app/features/<module>/pages/<module>-list-page.component.ts
src/app/features/<module>/pages/<module>-list-page.component.html
src/app/features/<module>/pages/<module>-form-page.component.ts
src/app/features/<module>/pages/<module>-form-page.component.html
src/app/features/<module>/pages/<module>-detail-page.component.html
```

For routes/menu:

```txt
src/app/app.routes.ts
src/app/core/config/app-navigation.ts
```

## 2. Replace Names Cheat Sheet

When copying from Staff to Student:

```txt
Staff -> Student
staff -> student
StaffDTO -> StudentDTO
StaffRecord -> StudentRecord
staffService -> studentService
staffRepository -> studentRepository
staffMapper -> studentMapper
/staff -> /students
features/staff -> features/students
```

When copying from Subject to Activity:

```txt
Subject -> Activity
subject -> activity
SubjectDTO -> ActivityDTO
SubjectRecord -> ActivityRecord
subjectService -> activityService
subjectRepository -> activityRepository
/subjects -> /activities
```

## 3. Add New Field To Existing Module

Example request:

```txt
Add NIC field to Staff.
Add blood group field to Student.
Add classroom/location field to Class.
```

Files:

```txt
Backend:
entity/Staff.java
dto/StaffDTO.java
mapper/StaffMapper.java
service/impl/StaffServiceImpl.java only if validation/default needed
src/test/java/org/edu/service/impl/StaffServiceImplTest.java

Frontend:
features/staff/models/staff.model.ts
features/staff/pages/staff-form-page.component.ts
features/staff/pages/staff-form-page.component.html
features/staff/pages/staff-detail-page.component.html
features/staff/pages/staff-list-page.component.html if table column needed
```

Backend entity:

```java
@Column(length = 20)
private String nic;
```

Backend DTO:

```java
@Pattern(regexp = "^(?:$|[0-9]{9}[vVxX]|[0-9]{12})$", message = "NIC must be valid")
private String nic;
```

Frontend model:

```ts
nic: string | null;
```

Frontend form group:

```ts
nic: ['', [Validators.pattern(/^(?:$|[0-9]{9}[vVxX]|[0-9]{12})$/)]],
```

Frontend HTML:

```html
<mat-form-field appearance="outline">
  <mat-label>NIC</mat-label>
  <input matInput formControlName="nic" />
</mat-form-field>
```

Request payload:

```ts
nic: this.normalizeText(formValue.nic),
```

Patch edit form:

```ts
nic: staff.nic ?? '',
```

Detail page:

```html
<div>
  <dt>NIC</dt>
  <dd>{{ selectedStaff.nic || '-' }}</dd>
</div>
```

## 4. Make Field Required

Example:

```txt
Make staff NIC required.
Make student phone number optional/required.
```

Backend DTO:

```java
@NotBlank(message = "NIC is required")
@Pattern(regexp = "^(?:[0-9]{9}[vVxX]|[0-9]{12})$", message = "NIC must be valid")
private String nic;
```

Frontend TS:

```ts
nic: ['', [Validators.required, Validators.pattern(/^(?:[0-9]{9}[vVxX]|[0-9]{12})$/)]],
```

Frontend HTML:

```html
<mat-error>NIC is required.</mat-error>
```

Make optional:

```txt
Remove @NotBlank/@NotNull from backend DTO.
Remove Validators.required from frontend form group.
```

## 5. Add GET Filter API

Example request:

```txt
Add filters to Students.
Filter Staff by department/status/category.
Filter Exams by class/subject/date/status.
```

Files:

```txt
Backend:
repository/StudentRepository.java
service/StudentService.java
service/impl/StudentServiceImpl.java
controller/StudentController.java

Frontend:
features/students/models/student.model.ts
features/students/services/student-api.service.ts
features/students/pages/student-list-page.component.ts
features/students/pages/student-list-page.component.html
features/students/pages/student-list-page.component.scss
```

Backend controller:

```java
@GetMapping("/filter")
@Operation(summary = "Filter students")
public Page<StudentDTO> filterStudents(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long classId,
        @RequestParam(required = false) Boolean active,
        Pageable pageable
) {
    return studentService.filterStudents(keyword, classId, active, pageable);
}
```

Service interface:

```java
Page<StudentDTO> filterStudents(String keyword, Long classId, Boolean active, Pageable pageable);
```

ServiceImpl:

```java
@Override
@Transactional(readOnly = true)
public Page<StudentDTO> filterStudents(String keyword, Long classId, Boolean active, Pageable pageable) {
    String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
    return studentRepository
            .filterStudents(normalizedKeyword, classId, active, pageable)
            .map(studentMapper::toDTO);
}
```

Repository:

```java
@Query("""
        select student
        from Student student
        where (:keyword is null
          or lower(student.name) like lower(concat('%', :keyword, '%'))
          or lower(student.studentId) like lower(concat('%', :keyword, '%')))
          and (:classId is null or student.currentClass.id = :classId)
          and (:active is null or student.active = :active)
        """)
Page<Student> filterStudents(
        @Param("keyword") String keyword,
        @Param("classId") Long classId,
        @Param("active") Boolean active,
        Pageable pageable
);
```

Frontend model:

```ts
export interface StudentFilter {
  keyword: string;
  classId: number | null;
  active: boolean | null;
}
```

Frontend API service:

```ts
filterStudents(filter: StudentFilter): Observable<PageResponse<StudentRecord>> {
  let params = new HttpParams()
    .set('page', 0)
    .set('size', 50)
    .set('sort', 'name,asc');

  if (filter.keyword.trim()) {
    params = params.set('keyword', filter.keyword.trim());
  }
  if (filter.classId !== null) {
    params = params.set('classId', filter.classId);
  }
  if (filter.active !== null) {
    params = params.set('active', filter.active);
  }

  return this.http.get<PageResponse<StudentRecord>>(`${this.apiUrl}/students/filter`, { params });
}
```

Frontend list TS:

```ts
protected readonly filterForm = this.formBuilder.nonNullable.group({
  keyword: [''],
  classId: this.formBuilder.control<number | null>(null),
  active: this.formBuilder.control<boolean | null>(true)
});

protected loadStudents(): void {
  this.loading.set(true);
  this.errorMessage.set('');

  this.studentApi.filterStudents(this.filterForm.getRawValue()).subscribe({
    next: (page) => {
      this.students.set(page.content);
      this.loading.set(false);
    },
    error: () => {
      this.errorMessage.set('Could not load students.');
      this.loading.set(false);
    }
  });
}

protected clearFilters(): void {
  this.filterForm.reset({
    keyword: '',
    classId: null,
    active: true
  });
  this.loadStudents();
}
```

Frontend list HTML:

```html
<div class="module-toolbar" [formGroup]="filterForm">
  <div class="toolbar-filters">
    <mat-form-field appearance="outline">
      <mat-label>Search students</mat-label>
      <input matInput formControlName="keyword" placeholder="Name or ID" />
    </mat-form-field>

    <mat-form-field appearance="outline">
      <mat-label>Status</mat-label>
      <mat-select formControlName="active">
        <mat-option [value]="null">All statuses</mat-option>
        <mat-option [value]="true">Active</mat-option>
        <mat-option [value]="false">Inactive</mat-option>
      </mat-select>
    </mat-form-field>
  </div>

  <div class="toolbar-actions">
    <button mat-stroked-button type="button" (click)="loadStudents()">
      <mat-icon>filter_alt</mat-icon>
      Apply
    </button>
    <button mat-stroked-button type="button" (click)="clearFilters()">
      <mat-icon>clear</mat-icon>
      Clear
    </button>
  </div>
</div>
```

## 6. Add Search API Only

Backend controller:

```java
@GetMapping("/search")
public Page<SubjectDTO> searchSubjects(@RequestParam String keyword, Pageable pageable) {
    return subjectService.searchSubjects(keyword, pageable);
}
```

Repository:

```java
Page<Subject> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
        String name,
        String code,
        Pageable pageable
);
```

ServiceImpl:

```java
@Override
@Transactional(readOnly = true)
public Page<SubjectDTO> searchSubjects(String keyword, Pageable pageable) {
    return subjectRepository
            .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword, pageable)
            .map(subjectMapper::toDTO);
}
```

Frontend API:

```ts
searchSubjects(keyword: string, page = 0, size = 20): Observable<PageResponse<SubjectRecord>> {
  const params = new HttpParams()
    .set('keyword', keyword)
    .set('page', page)
    .set('size', size)
    .set('sort', 'name,asc');

  return this.http.get<PageResponse<SubjectRecord>>(`${this.apiUrl}/subjects/search`, { params });
}
```

## 7. Active List API For Dropdowns

Use this when dropdown needs active records.

Backend controller:

```java
@GetMapping("/active")
public List<ClassDTO> getActiveClasses() {
    return classService.getAllActiveClasses();
}
```

Service interface:

```java
List<ClassDTO> getAllActiveClasses();
```

Repository:

```java
List<Class> findByActiveTrue();
```

ServiceImpl:

```java
@Override
@Transactional(readOnly = true)
public List<ClassDTO> getAllActiveClasses() {
    return classRepository.findByActiveTrue()
            .stream()
            .map(classMapper::toDTO)
            .toList();
}
```

Frontend API:

```ts
getActiveClasses(): Observable<ClassOption[]> {
  return this.http.get<ClassOption[]>(`${this.apiUrl}/classes/active`);
}
```

Frontend load with `forkJoin`:

```ts
forkJoin({
  classes: this.api.getActiveClasses(),
  staff: this.api.getActiveStaff()
}).subscribe({
  next: ({ classes, staff }) => {
    this.classes.set(classes);
    this.staff.set(staff);
  },
  error: () => this.errorMessage.set('Could not load form data.')
});
```

## 8. Add Create API

Example request:

```txt
Add Create Grade API.
```

Files:

```txt
Backend:
src/main/java/org/edu/controller/GradeController.java
src/main/java/org/edu/service/GradeService.java
src/main/java/org/edu/service/impl/GradeServiceImpl.java

Frontend:
src/app/features/grades/models/grade.model.ts
src/app/features/grades/services/grade-api.service.ts
src/app/features/grades/pages/grade-form-page.component.ts
```

Backend controller:

```java
@PostMapping
@Operation(summary = "Create a grade")
public GradeDTO createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
    return gradeService.createGrade(gradeDTO);
}
```

Backend service interface:

```java
GradeDTO createGrade(GradeDTO gradeDTO);
```

Backend service implementation:

```java
@Override
public GradeDTO createGrade(GradeDTO gradeDTO) {
    validateDuplicateName(gradeDTO.getName(), null);
    validateDuplicateLevel(gradeDTO.getLevel(), null);

    Grade grade = new Grade();
    applyFields(grade, gradeDTO);
    grade.setActive(true);
    return toDTO(gradeRepository.save(grade));
}
```

Required helper methods in same ServiceImpl:

```java
private void applyFields(Grade grade, GradeDTO dto) {
    grade.setName(dto.getName().trim());
    grade.setLevel(dto.getLevel());
}

private void validateDuplicateName(String name, Long currentId) {
    boolean exists = currentId == null
            ? gradeRepository.existsByNameIgnoreCase(name.trim())
            : gradeRepository.existsByNameIgnoreCaseAndIdNot(name.trim(), currentId);
    if (exists) {
        throw new IllegalStateException("Grade name already exists");
    }
}

private void validateDuplicateLevel(Integer level, Long currentId) {
    boolean exists = currentId == null
            ? gradeRepository.existsByLevel(level)
            : gradeRepository.existsByLevelAndIdNot(level, currentId);
    if (exists) {
        throw new IllegalStateException("Grade level already exists");
    }
}

private GradeDTO toDTO(Grade grade) {
    GradeDTO dto = new GradeDTO();
    dto.setId(grade.getId());
    dto.setName(grade.getName());
    dto.setLevel(grade.getLevel());
    dto.setActive(grade.isActive());
    return dto;
}
```

Frontend model:

```ts
export interface GradeSaveRequest {
  name: string;
  level: number;
  active: boolean;
}
```

Frontend API service:

```ts
createGrade(request: GradeSaveRequest): Observable<GradeRecord> {
  return this.http.post<GradeRecord>(`${this.apiUrl}/grades`, request);
}
```

Frontend form save:

```ts
protected saveGrade(): void {
  if (this.gradeForm.invalid) {
    this.gradeForm.markAllAsTouched();
    return;
  }

  this.saving.set(true);
  this.errorMessage.set('');

  const formValue = this.gradeForm.getRawValue();
  const request: GradeSaveRequest = {
    name: formValue.name.trim(),
    level: formValue.level,
    active: formValue.active
  };

  this.gradeApi.createGrade(request).subscribe({
    next: (grade) => this.router.navigate(['/grades', grade.id]),
    error: () => {
      this.errorMessage.set('Could not save grade.');
      this.saving.set(false);
    }
  });
}
```

## 9. Add Update API

Controller:

```java
@PatchMapping("/{id}")
public GradeDTO updateGrade(@PathVariable Long id, @Valid @RequestBody GradeDTO dto) {
    return gradeService.updateGrade(id, dto);
}
```

Service interface:

```java
GradeDTO updateGrade(Long id, GradeDTO dto);
```

ServiceImpl:

```java
@Override
public GradeDTO updateGrade(Long id, GradeDTO dto) {
    Grade grade = gradeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

    gradeMapper.updateEntityFromDTO(dto, grade);
    return gradeMapper.toDTO(gradeRepository.save(grade));
}
```

Frontend API:

```ts
updateGrade(id: number, request: GradeSaveRequest): Observable<GradeRecord> {
  return this.http.patch<GradeRecord>(`${this.apiUrl}/grades/${id}`, request);
}
```

Frontend create/edit save:

```ts
const request = this.form.getRawValue();
const saveRequest = this.entryId
  ? this.api.updateGrade(this.entryId, request)
  : this.api.createGrade(request);

saveRequest.subscribe({
  next: (grade) => this.router.navigate(['/grades', grade.id]),
  error: () => {
    this.errorMessage.set('Could not save grade.');
    this.saving.set(false);
  }
});
```

## 10. Soft Delete / Deactivate

Use when examiner says "delete karanna epa, inactive karanna".

Controller:

```java
@DeleteMapping("/{id}")
public void deleteStaff(@PathVariable Long id) {
    staffService.deleteStaff(id);
}
```

ServiceImpl:

```java
@Override
public void deleteStaff(Long id) {
    Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

    if (!staff.isActive()) {
        throw new IllegalStateException("Staff already inactive");
    }

    staff.setActive(false);
    staffRepository.save(staff);
}
```

Frontend API:

```ts
deleteStaff(id: number): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/staff/${id}`);
}
```

Frontend list TS:

```ts
protected deactivateStaff(item: StaffRecord): void {
  if (!window.confirm(`Deactivate staff member "${item.name}"?`)) {
    return;
  }

  this.staffApi.deleteStaff(item.id).subscribe({
    next: () => this.loadStaff(),
    error: () => this.errorMessage.set('Could not deactivate staff member.')
  });
}
```

## 11. Hard Delete With Child Cleanup

Use when delete fails due to linked data.

Repository child delete:

```java
void deleteByStudentId(Long studentId);
```

Repository child unlink:

```java
@Modifying
@Query("update Attendance attendance set attendance.timetable = null where attendance.timetable.id = :timetableId")
void detachTimetable(@Param("timetableId") Long timetableId);
```

ServiceImpl:

```java
@Override
public void deleteTimetable(Long id) {
    if (!timetableRepository.existsById(id)) {
        throw new ResourceNotFoundException("Timetable entry not found");
    }

    attendanceRepository.detachTimetable(id);
    teacherLeaveSessionRepository.deleteByTimetableId(id);
    timetableRepository.deleteById(id);
}
```

## 12. Restore / Activate Record

Controller:

```java
@PostMapping("/{id}/activate")
public void activateStaff(@PathVariable Long id) {
    staffService.activateStaff(id);
}
```

Service:

```java
void activateStaff(Long id);
```

ServiceImpl:

```java
@Override
public void activateStaff(Long id) {
    Staff staff = staffRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    staff.setActive(true);
    staffRepository.save(staff);
}
```

Frontend API:

```ts
activateStaff(id: number): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/staff/${id}/activate`, {});
}
```

Frontend button:

```html
<button mat-icon-button type="button" aria-label="Activate" (click)="activateStaff(item)">
  <mat-icon>check_circle</mat-icon>
</button>
```

## 13. Approve / Reject Status API

Example:

```txt
Approve leave request.
Reject teacher leave.
Publish report.
```

Controller:

```java
@PostMapping("/{id}/approve")
public LeaveRequestDTO approveLeave(@PathVariable Long id, @Valid @RequestBody LeaveRequestReviewRequest request) {
    return leaveRequestService.approveLeave(id, request);
}

@PostMapping("/{id}/reject")
public LeaveRequestDTO rejectLeave(@PathVariable Long id, @Valid @RequestBody LeaveRequestReviewRequest request) {
    return leaveRequestService.rejectLeave(id, request);
}
```

ServiceImpl:

```java
@Override
public LeaveRequestDTO approveLeave(Long id, LeaveRequestReviewRequest request) {
    LeaveRequest leave = leaveRequestRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

    if (leave.getStatus() != LeaveStatus.PENDING) {
        throw new IllegalStateException("Only pending leave requests can be approved");
    }

    leave.setStatus(LeaveStatus.APPROVED);
    leave.setReviewerRemarks(trimToNull(request.getRemarks()));
    leave.setReviewedAt(LocalDateTime.now());

    return leaveRequestMapper.toDTO(leaveRequestRepository.save(leave));
}
```

Frontend API:

```ts
approveLeave(id: number, request: LeaveReviewRequest): Observable<LeaveRequestRecord> {
  return this.http.post<LeaveRequestRecord>(`${this.apiUrl}/leave-requests/${id}/approve`, request);
}
```

Frontend action:

```ts
protected approveLeave(item: LeaveRequestRecord): void {
  this.leaveApi.approveLeave(item.id, { remarks: this.reviewRemarks() }).subscribe({
    next: (updated) => this.leave.set(updated),
    error: () => this.errorMessage.set('Could not approve leave request.')
  });
}
```

## 14. Duplicate Validation

Example:

```txt
Prevent duplicate grade level.
Prevent duplicate subject code.
Prevent duplicate student ID.
```

Repository:

```java
boolean existsByCodeIgnoreCase(String code);

boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
```

Create service:

```java
if (subjectRepository.existsByCodeIgnoreCase(dto.getCode().trim())) {
    throw new IllegalArgumentException("Subject code already exists");
}
```

Update service:

```java
if (subjectRepository.existsByCodeIgnoreCaseAndIdNot(dto.getCode().trim(), id)) {
    throw new IllegalArgumentException("Subject code already exists");
}
```

## 15. Role Access Change

Example:

```txt
Allow TEACHER to access Timetable.
Hide Users from non-admin.
Allow STAFF to see Notices.
```

Backend controller:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
```

Frontend `app.routes.ts`:

```ts
{
  path: 'timetable',
  canActivate: [roleGuard],
  data: { allowedRoles: ['ADMIN', 'TEACHER'] },
  loadChildren: () => import('./features/timetable/timetable.routes').then((m) => m.TIMETABLE_ROUTES)
}
```

Frontend `app-navigation.ts`:

```ts
{
  label: 'Timetable',
  icon: 'calendar_view_week',
  route: '/timetable',
  description: 'Class and teacher schedules',
  allowedRoles: ['ADMIN', 'TEACHER']
}
```

## 16. Add New Dropdown Option

Files:

```txt
Backend enum under org.edu.util if stored in DB
Frontend config file under features/<module>/config/*.options.ts
```

Backend enum:

```java
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED,
    HALF_DAY
}
```

Frontend options:

```ts
export const ATTENDANCE_STATUS_OPTIONS = [
  { value: 'PRESENT', label: 'Present' },
  { value: 'ABSENT', label: 'Absent' },
  { value: 'LATE', label: 'Late' },
  { value: 'EXCUSED', label: 'Excused' },
  { value: 'HALF_DAY', label: 'Half Day' }
] as const;
```

HTML:

```html
@for (status of statusOptions; track status.value) {
  <mat-option [value]="status.value">{{ status.label }}</mat-option>
}
```

## 17. Add Table Column

Frontend list TS:

```ts
protected readonly displayedColumns = ['name', 'staffId', 'nic', 'status', 'actions'];
```

Frontend list HTML:

```html
<ng-container matColumnDef="nic">
  <th mat-header-cell *matHeaderCellDef>NIC</th>
  <td mat-cell *matCellDef="let item">{{ item.nic || '-' }}</td>
</ng-container>
```

If error says:

```txt
Could not find column with id "nic"
```

Then `displayedColumns` and `matColumnDef` names do not match.

## 18. Remove Field From UI

Example:

```txt
Remove room number from Timetable form/list/detail.
```

Files:

```txt
features/timetable/pages/timetable-form-page.component.ts
features/timetable/pages/timetable-form-page.component.html
features/timetable/pages/timetable-list-page.component.ts
features/timetable/pages/timetable-list-page.component.html
features/timetable/pages/timetable-detail-page.component.html
features/timetable/models/timetable.model.ts if request type changes
```

Remove from form group:

```ts
// remove this
roomNumber: ['']
```

Remove from payload or send null:

```ts
roomNumber: null
```

Remove HTML field:

```html
<!-- remove mat-form-field for Room number -->
```

Remove table column:

```ts
protected readonly displayedColumns = ['day', 'time', 'class', 'subject', 'teacher', 'actions'];
```

## 19. Add Button Action

HTML:

```html
<button mat-icon-button type="button" aria-label="Archive" (click)="archiveItem(item)">
  <mat-icon>archive</mat-icon>
</button>
```

TS:

```ts
protected archiveItem(item: NoticeRecord): void {
  if (!window.confirm(`Archive "${item.title}"?`)) {
    return;
  }

  this.noticeApi.archiveNotice(item.id).subscribe({
    next: () => this.loadNotices(),
    error: () => this.errorMessage.set('Could not archive notice.')
  });
}
```

API:

```ts
archiveNotice(id: number): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/notices/${id}/archive`, {});
}
```

Backend controller:

```java
@PostMapping("/{id}/archive")
public void archiveNotice(@PathVariable Long id) {
    noticeService.archiveNotice(id);
}
```

## 20. Add Summary Card Count

TS:

```ts
protected readonly activeCount = computed(() => this.staff().filter((item) => item.active).length);
protected readonly inactiveCount = computed(() => this.staff().filter((item) => !item.active).length);
```

HTML:

```html
<mat-card class="summary-card success">
  <span>Active</span>
  <strong>{{ activeCount() }}</strong>
</mat-card>
```

SCSS:

```scss
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
```

## 21. Add New Route/Menu Item

Frontend new route file:

```ts
import { Routes } from '@angular/router';
import { LibraryListPageComponent } from './pages/library-list-page.component';

export const LIBRARY_ROUTES: Routes = [
  {
    path: '',
    component: LibraryListPageComponent
  }
];
```

Add to `app.routes.ts`:

```ts
{
  path: 'library',
  canActivate: [roleGuard],
  data: { allowedRoles: ['ADMIN'] },
  loadChildren: () => import('./features/library/library.routes').then((m) => m.LIBRARY_ROUTES)
}
```

Add to `app-navigation.ts`:

```ts
{
  label: 'Library',
  icon: 'local_library',
  route: '/library',
  description: 'Books and borrowing records',
  allowedRoles: ['ADMIN']
}
```

## 22. Add Completely New CRUD Module

Example:

```txt
Add Library Books module.
```

Backend files to create:

```txt
entity/LibraryBook.java
dto/LibraryBookDTO.java
repository/LibraryBookRepository.java
service/LibraryBookService.java
service/impl/LibraryBookServiceImpl.java
controller/LibraryBookController.java
mapper/LibraryBookMapper.java if using mapper
```

Entity:

```java
@Entity
@Table(name = "library_books")
@Getter
@Setter
@NoArgsConstructor
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 120)
    private String author;

    @Column(nullable = false, unique = true, length = 50)
    private String isbn;

    @Column(nullable = false)
    private boolean active = true;
}
```

DTO:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBookDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    private boolean active;
}
```

Repository:

```java
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    Page<LibraryBook> findByActiveTrue(Pageable pageable);
    boolean existsByIsbnIgnoreCase(String isbn);
}
```

Service:

```java
public interface LibraryBookService {
    LibraryBookDTO createBook(LibraryBookDTO dto);
    LibraryBookDTO updateBook(Long id, LibraryBookDTO dto);
    void deleteBook(Long id);
    LibraryBookDTO getBookById(Long id);
    Page<LibraryBookDTO> getBooks(Pageable pageable);
}
```

Controller:

```java
@RestController
@RequestMapping("/api/library-books")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LibraryBookController {

    private final LibraryBookService libraryBookService;

    @PostMapping
    public LibraryBookDTO createBook(@Valid @RequestBody LibraryBookDTO dto) {
        return libraryBookService.createBook(dto);
    }

    @PatchMapping("/{id}")
    public LibraryBookDTO updateBook(@PathVariable Long id, @Valid @RequestBody LibraryBookDTO dto) {
        return libraryBookService.updateBook(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        libraryBookService.deleteBook(id);
    }

    @GetMapping("/{id}")
    public LibraryBookDTO getBookById(@PathVariable Long id) {
        return libraryBookService.getBookById(id);
    }

    @GetMapping
    public Page<LibraryBookDTO> getBooks(Pageable pageable) {
        return libraryBookService.getBooks(pageable);
    }
}
```

Frontend files to create:

```txt
features/library/models/library.model.ts
features/library/services/library-api.service.ts
features/library/pages/library-list-page.component.ts/html/scss
features/library/pages/library-form-page.component.ts/html/scss
features/library/library.routes.ts
```

Frontend model:

```ts
export interface LibraryBookRecord {
  id: number;
  title: string;
  author: string;
  isbn: string;
  active: boolean;
}

export interface LibraryBookSaveRequest {
  title: string;
  author: string;
  isbn: string;
}
```

Frontend API:

```ts
@Injectable({ providedIn: 'root' })
export class LibraryApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = API_BASE_URL;

  getBooks(page = 0, size = 20): Observable<PageResponse<LibraryBookRecord>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'title,asc');
    return this.http.get<PageResponse<LibraryBookRecord>>(`${this.apiUrl}/library-books`, { params });
  }

  createBook(request: LibraryBookSaveRequest): Observable<LibraryBookRecord> {
    return this.http.post<LibraryBookRecord>(`${this.apiUrl}/library-books`, request);
  }

  updateBook(id: number, request: LibraryBookSaveRequest): Observable<LibraryBookRecord> {
    return this.http.patch<LibraryBookRecord>(`${this.apiUrl}/library-books/${id}`, request);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/library-books/${id}`);
  }
}
```

## 23. Add Audit Log To Action

Controller usually has `AuditLogService`.

```java
private final AuditLogService auditLogService;
```

After create:

```java
auditLogService.log(
        AuditAction.CREATE,
        AuditEntityType.STAFF,
        response.getId(),
        response.getName(),
        "Created staff record"
);
```

After update:

```java
auditLogService.log(
        AuditAction.UPDATE,
        AuditEntityType.STAFF,
        response.getId(),
        response.getName(),
        "Updated staff record"
);
```

Need enum update if new entity type:

```txt
org.edu.util.AuditEntityType
org.edu.util.AuditAction
```

## 24. Change School Time Rule

Files:

```txt
Backend:
service/SchoolDayPolicyService.java
service/impl/SchoolDayPolicyServiceImpl.java
service/impl/SystemSettingsServiceImpl.java
dto/request/SystemSettingsUpdateRequest.java

Frontend:
features/system-settings/pages/system-settings-page.component.ts/html
features/timetable/pages/timetable-form-page.component.ts/html
```

Default values:

```java
LocalTime DEFAULT_START_TIME = LocalTime.of(7, 30);
LocalTime DEFAULT_END_TIME = LocalTime.of(13, 30);
```

Validation:

```java
if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
    throw new IllegalArgumentException(activityName + " requires a valid start and end time");
}

if (startTime.isBefore(schoolStart) || endTime.isAfter(schoolEnd)) {
    throw new IllegalArgumentException(activityName + " must be between " + schoolStart + " and " + schoolEnd);
}
```

Frontend default:

```ts
protected readonly schoolStartTime = signal('07:30');
protected readonly schoolEndTime = signal('13:30');
```

HTML time input:

```html
<input matInput type="time" formControlName="startTime" [min]="schoolStartTime()" [max]="schoolEndTime()" />
```

## 25. Fix Save Button Stuck

Problem:

```txt
saving.set(true) called before frontend validation returns.
```

Fix:

```ts
if (invalidCondition) {
  this.errorMessage.set('Error message.');
  this.saving.set(false);
  return;
}
```

Better pattern:

```ts
if (this.form.invalid) {
  this.form.markAllAsTouched();
  return;
}

const formValue = this.form.getRawValue();

if (invalidBusinessRule) {
  this.errorMessage.set('Business rule failed.');
  return;
}

this.saving.set(true);
```

## 26. Fix Teacher Dropdown Empty

Files:

```txt
features/timetable/pages/timetable-form-page.component.ts
features/timetable/pages/timetable-form-page.component.html
features/timetable/models/timetable.model.ts
Backend service/impl/TimetableServiceImpl.java if backend blocks save
```

Frontend method:

```ts
protected allowedStaff(): TimetableStaffOption[] {
  const subjectId = this.timetableForm.controls.subjectId.value;
  const selectedStaffId = this.timetableForm.controls.staffId.value;
  const exactAssignedStaffIds = new Set(
    this.assignments()
      .filter((assignment) => subjectId === null || assignment.subjectId === subjectId)
      .map((assignment) => assignment.staffId)
  );

  const assignedStaff = this.staff().filter((staffMember) => {
    const isCurrentSelection = selectedStaffId !== null && staffMember.id === selectedStaffId;
    return isCurrentSelection || exactAssignedStaffIds.has(staffMember.id);
  });

  if (assignedStaff.length > 0) {
    return assignedStaff;
  }

  const teachingCapableStaff = this.staff().filter((staffMember) => staffMember.teachingCapable !== false);
  return teachingCapableStaff.length > 0 ? teachingCapableStaff : this.staff();
}
```

Backend: remove too strict teacher assignment validation if needed:

```java
// Do not block timetable save only because teaching assignment is missing.
// Still validate active staff, class, subject, time conflict.
```

## 27. Add Dashboard Card

Backend dashboard DTO:

```txt
dto/DashboardDTO.java OR feature-specific dashboard DTO
service/impl/DashboardServiceImpl.java
controller/DashboardController.java
repository needed for count query
```

Repository:

```java
long countByActiveTrue();
```

DTO:

```java
private long activeStaffCount;
```

Frontend model:

```ts
activeStaffCount: number;
```

Dashboard HTML:

```html
<mat-card class="metric-card">
  <span>Active Staff</span>
  <strong>{{ dashboard()?.activeStaffCount ?? 0 }}</strong>
</mat-card>
```

## 28. Add Analytics Chart

Frontend files:

```txt
features/analytics/models/analytics.model.ts
features/analytics/services/analytics-api.service.ts
features/analytics/pages/analytics-page.component.ts/html/scss
shared/ui/chart-card/*
shared/util/chart-colors.ts
```

Backend files:

```txt
controller/AnalyticsController.java if exists or create
service/AnalyticsService.java
service/impl/AnalyticsServiceImpl.java
repository queries for counts
dto/AnalyticsDTO.java
```

DTO:

```java
public record MonthlyCountDTO(String label, long count) {
}
```

Repository query:

```java
@Query("""
        select count(student)
        from Student student
        where student.active = true
        """)
long countActiveStudents();
```

Frontend chart input:

```ts
protected readonly attendanceChart = computed(() => ({
  labels: this.analytics()?.attendanceByMonth.map((item) => item.label) ?? [],
  datasets: [
    {
      label: 'Attendance',
      data: this.analytics()?.attendanceByMonth.map((item) => item.count) ?? []
    }
  ]
}));
```

## 29. Add File Upload API

Backend request:

```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public DocumentDTO createDocument(
        @RequestPart("metadata") @Valid DocumentCreateRequest request,
        @RequestPart("file") MultipartFile file
) {
    return documentService.createDocument(request, file);
}
```

Frontend:

```ts
createDocument(request: DocumentCreateRequest, file: File): Observable<DocumentRecord> {
  const formData = new FormData();
  formData.append('metadata', new Blob([JSON.stringify(request)], { type: 'application/json' }));
  formData.append('file', file);

  return this.http.post<DocumentRecord>(`${this.apiUrl}/documents`, formData);
}
```

HTML:

```html
<input type="file" (change)="onFileSelected($event)" />
```

TS:

```ts
protected selectedFile = signal<File | null>(null);

protected onFileSelected(event: Event): void {
  const input = event.target as HTMLInputElement;
  this.selectedFile.set(input.files?.[0] ?? null);
}
```

## 30. Add PDF/Report Generation Button

Backend controller:

```java
@GetMapping("/{id}/pdf")
public ResponseEntity<byte[]> downloadReportPdf(@PathVariable Long id) {
    byte[] pdf = academicReportPdfService.generateReportPdf(id);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
}
```

Frontend API:

```ts
downloadReportPdf(id: number): Observable<Blob> {
  return this.http.get(`${this.apiUrl}/academic-reports/${id}/pdf`, {
    responseType: 'blob'
  });
}
```

Frontend button action:

```ts
protected downloadPdf(report: ReportRecord): void {
  this.reportApi.downloadReportPdf(report.id).subscribe({
    next: (blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `report-${report.id}.pdf`;
      link.click();
      URL.revokeObjectURL(url);
    },
    error: () => this.errorMessage.set('Could not download PDF.')
  });
}
```

## 31. Add Parent Portal Field

Example:

```txt
Show student attendance percentage in parent portal.
```

Backend files:

```txt
dto/parentportal/ParentPortalStudentDetailDTO.java
service/impl/ParentPortalServiceImpl.java
repository/AttendanceRepository.java if query needed
controller/ParentPortalController.java if new endpoint needed
```

DTO field:

```java
private BigDecimal attendancePercentage;
```

Service calculation:

```java
long total = attendanceRecords.size();
long present = attendanceRecords.stream()
        .filter(record -> record.getStatus() == AttendanceStatus.PRESENT)
        .count();
BigDecimal percentage = total == 0
        ? BigDecimal.ZERO
        : BigDecimal.valueOf(present)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
```

Frontend files:

```txt
features/parent-portal/models/parent-portal.model.ts
features/parent-portal/pages/parent-student-detail-page.component.ts/html
```

Frontend display:

```html
<mat-card class="summary-card">
  <span>Attendance</span>
  <strong>{{ student()?.attendancePercentage ?? 0 }}%</strong>
</mat-card>
```

## 32. Add Teacher Portal Field

Example:

```txt
Show today timetable count on teacher dashboard.
```

Backend files:

```txt
dto/teacherportal/TeacherPortalDashboardDTO.java
service/impl/TeacherPortalServiceImpl.java
repository/TimetableRepository.java
controller/TeacherPortalController.java
```

Repository:

```java
long countByStaffIdAndDayOfWeek(Long staffId, DayOfWeek dayOfWeek);
```

Service:

```java
dashboard.setTodayScheduleCount(
        timetableRepository.countByStaffIdAndDayOfWeek(staff.getId(), LocalDate.now().getDayOfWeek())
);
```

Frontend:

```txt
features/teacher-portal/models/teacher-portal.model.ts
features/teacher-portal/pages/teacher-portal-dashboard-page.component.html
```

## 33. Add Sorting

Backend: use Pageable sort from frontend.

Frontend:

```ts
const params = new HttpParams()
  .set('page', page)
  .set('size', size)
  .set('sort', 'name,asc');
```

Change sort:

```ts
.set('sort', 'createdAt,desc')
```

Backend repository methods with order:

```java
List<Timetable> findByStudentClassIdOrderByDayOfWeekAscStartTimeAsc(Long classId);
```

## 34. Add Pagination

Backend controller:

```java
@GetMapping
public Page<StudentDTO> getStudents(Pageable pageable) {
    return studentService.getStudents(pageable);
}
```

Frontend API:

```ts
getStudents(page = 0, size = 20): Observable<PageResponse<StudentRecord>> {
  const params = new HttpParams()
    .set('page', page)
    .set('size', size)
    .set('sort', 'name,asc');

  return this.http.get<PageResponse<StudentRecord>>(`${this.apiUrl}/students`, { params });
}
```

Frontend state:

```ts
protected readonly pageIndex = signal(0);
protected readonly pageSize = signal(20);
protected readonly totalElements = signal(0);
```

On response:

```ts
this.items.set(page.content);
this.totalElements.set(page.totalElements);
```

## 35. Fix API 400/401/403/404/500

400:

```txt
Request body wrong, validation failed, enum value wrong, required field missing.
Check DTO/request and frontend payload.
```

401:

```txt
Token missing/expired.
Check auth.interceptor.ts and localStorage token.
```

403:

```txt
Role not allowed.
Check @PreAuthorize, app.routes.ts, app-navigation.ts.
```

404:

```txt
Endpoint URL mismatch.
Check frontend API service URL and backend @RequestMapping/@GetMapping.
```

500:

```txt
Backend exception, DB relation, null pointer.
Check service/impl and terminal logs.
```

## 36. Fix CORS / HTTP Method Block

File:

```txt
backend/security/SecurityConfig.java
```

Allowed methods:

```java
configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

Frontend uses:

```ts
this.http.patch(...)
this.http.put(...)
this.http.delete(...)
```

If CORS blocks PUT/PATCH, add method above.

## 37. Add Global Error Message Display

Frontend list/form TS:

```ts
protected readonly errorMessage = signal('');
```

HTML:

```html
@if (errorMessage()) {
  <div class="error-banner">{{ errorMessage() }}</div>
}
```

SCSS:

```scss
.error-banner {
  margin: 14px;
  padding: 10px 12px;
  border: 1px solid #fca5a5;
  border-radius: 8px;
  background: var(--app-danger-soft);
  color: var(--app-danger);
}
```

Backend structured error:

```java
throw new IllegalArgumentException("Student already exists");
```

Global handler already maps this to `INVALID_ARGUMENT`.

## 38. Add Success Message

TS:

```ts
protected readonly successMessage = signal('');
```

HTML:

```html
@if (successMessage()) {
  <div class="success-banner">{{ successMessage() }}</div>
}
```

On save:

```ts
next: (settings) => {
  this.settings.set(settings);
  this.successMessage.set('System settings updated.');
  this.saving.set(false);
}
```

## 39. Add Confirmation Dialog

Simple browser confirm:

```ts
if (!window.confirm(`Delete "${item.name}"?`)) {
  return;
}
```

Use before:

```txt
delete
deactivate
publish
approve/reject
reset password
```

## 40. Add Date Range Filter

Backend controller:

```java
@GetMapping("/filter")
public Page<AttendanceDTO> filterAttendance(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        Pageable pageable
) {
    return attendanceService.filterAttendance(from, to, pageable);
}
```

Repository:

```java
@Query("""
        select attendance
        from Attendance attendance
        where (:fromDate is null or attendance.attendanceDate >= :fromDate)
          and (:toDate is null or attendance.attendanceDate <= :toDate)
        """)
Page<Attendance> filterAttendance(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
);
```

Frontend HTML:

```html
<mat-form-field appearance="outline">
  <mat-label>From</mat-label>
  <input matInput type="date" formControlName="from" />
</mat-form-field>

<mat-form-field appearance="outline">
  <mat-label>To</mat-label>
  <input matInput type="date" formControlName="to" />
</mat-form-field>
```

Frontend API:

```ts
if (filter.from) {
  params = params.set('from', filter.from);
}
if (filter.to) {
  params = params.set('to', filter.to);
}
```

## 41. Add Enum Filter

Backend controller:

```java
@RequestParam(required = false) AttendanceStatus status
```

Repository:

```java
and (:status is null or attendance.status = :status)
```

Frontend model:

```ts
status: AttendanceStatus | '';
```

Frontend API:

```ts
if (filter.status) {
  params = params.set('status', filter.status);
}
```

HTML:

```html
<mat-select formControlName="status">
  <mat-option value="">All statuses</mat-option>
  @for (status of statusOptions; track status.value) {
    <mat-option [value]="status.value">{{ status.label }}</mat-option>
  }
</mat-select>
```

## 42. Add Login/User Role

Backend enum:

```txt
org.edu.util.Role
```

Add:

```java
LIBRARIAN
```

Frontend:

```txt
core/models/auth.model.ts
```

Add:

```ts
export type UserRole = 'ADMIN' | 'TEACHER' | 'STAFF' | 'PARENT' | 'STUDENT' | 'LIBRARIAN';
```

Routes/navigation:

```ts
allowedRoles: ['ADMIN', 'LIBRARIAN']
```

Backend controller:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
```

## 43. Add Password Reset / Change Password

Backend request:

```java
@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;
}
```

Controller:

```java
@PatchMapping("/{userId}/password")
public void changePassword(@PathVariable Long userId, @Valid @RequestBody PasswordChangeRequest request) {
    userService.changePassword(userId, request);
}
```

ServiceImpl:

```java
User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
user.setPassword(passwordEncoder.encode(request.getNewPassword()));
userRepository.save(user);
```

Frontend API:

```ts
changePassword(userId: number, request: PasswordChangeRequest): Observable<void> {
  return this.http.patch<void>(`${this.apiUrl}/users/${userId}/password`, request);
}
```

## 44. Add Count Endpoint

Backend controller:

```java
@GetMapping("/summary")
public StudentSummaryDTO getStudentSummary() {
    return studentService.getStudentSummary();
}
```

DTO:

```java
public record StudentSummaryDTO(long total, long active, long inactive) {
}
```

Repository:

```java
long countByActiveTrue();
long countByActiveFalse();
```

Service:

```java
return new StudentSummaryDTO(
        studentRepository.count(),
        studentRepository.countByActiveTrue(),
        studentRepository.countByActiveFalse()
);
```

Frontend API:

```ts
getStudentSummary(): Observable<StudentSummary> {
  return this.http.get<StudentSummary>(`${this.apiUrl}/students/summary`);
}
```

## 45. Add Bulk Action API

Example:

```txt
Bulk mark attendance.
Bulk activate students.
Bulk delete notices.
```

Request:

```java
@Getter
@Setter
public class BulkStatusRequest {
    @NotEmpty(message = "At least one id is required")
    private List<Long> ids;
}
```

Controller:

```java
@PostMapping("/bulk-deactivate")
public void bulkDeactivate(@Valid @RequestBody BulkStatusRequest request) {
    studentService.bulkDeactivate(request.getIds());
}
```

Service:

```java
@Override
public void bulkDeactivate(List<Long> ids) {
    List<Student> students = studentRepository.findAllById(ids);
    students.forEach(student -> student.setActive(false));
    studentRepository.saveAll(students);
}
```

Frontend:

```ts
bulkDeactivate(ids: number[]): Observable<void> {
  return this.http.post<void>(`${this.apiUrl}/students/bulk-deactivate`, { ids });
}
```

## 46. Add Subject/Class Relationship

Backend entity relationship usually exists in `Class.java` and `Subject.java`.

Service pattern:

```java
List<Subject> subjects = subjectRepository.findAllById(dto.getSubjectIds());
studentClass.setSubjects(new HashSet<>(subjects));
```

Frontend multi-select:

```html
<mat-form-field appearance="outline">
  <mat-label>Subjects</mat-label>
  <mat-select formControlName="subjectIds" multiple>
    @for (subject of subjects(); track subject.id) {
      <mat-option [value]="subject.id">{{ subject.code }} · {{ subject.name }}</mat-option>
    }
  </mat-select>
</mat-form-field>
```

TS form:

```ts
subjectIds: this.formBuilder.nonNullable.control<number[]>([])
```

Payload:

```ts
subjectIds: formValue.subjectIds
```

## 47. Add Frontend Responsive Filter Toolbar

SCSS:

```scss
.module-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  align-items: start;
  padding: 14px;
}

.toolbar-filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 10px;
  min-width: 0;
}

.toolbar-filters mat-form-field {
  width: 100%;
  min-width: 0;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

@media (max-width: 1100px) {
  .module-toolbar {
    grid-template-columns: 1fr;
  }

  .toolbar-actions {
    justify-content: flex-start;
  }
}
```

## 48. Add Empty State

HTML:

```html
@if (!loading() && items().length === 0) {
  <div class="empty-state">
    <mat-icon>inbox</mat-icon>
    <strong>No records found</strong>
    <span>Create the first record or change your filters.</span>
  </div>
}
```

SCSS:

```scss
.empty-state {
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 56px 20px;
  color: var(--app-text-muted);
  text-align: center;
}
```

## 49. Add Loading Progress

TS:

```ts
protected readonly loading = signal(false);
```

HTML:

```html
@if (loading()) {
  <mat-progress-bar mode="indeterminate" />
}
```

Before API:

```ts
this.loading.set(true);
```

On success/error:

```ts
this.loading.set(false);
```

## 50. Common Frontend Import Fixes

If using forms:

```ts
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
```

Component imports:

```ts
imports: [
  ReactiveFormsModule,
  MatButtonModule,
  MatCardModule,
  MatFormFieldModule,
  MatIconModule,
  MatInputModule,
  MatProgressBarModule,
  MatSelectModule,
  MatTableModule,
  PageHeaderComponent
]
```

If using date pipe:

```ts
import { DatePipe } from '@angular/common';
```

Add to component imports:

```ts
DatePipe
```

## 51. Common Backend Imports

Controller:

```java
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
```

ServiceImpl:

```java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
```

Repository custom query:

```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
```

Modifying query:

```java
import org.springframework.data.jpa.repository.Modifying;
```

Validation:

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
```

## 52. Common Error Fixes

Frontend:

```txt
Cannot find control with name 'x'
-> Add x to form group in component.ts.

Could not find column with id 'x'
-> Add matColumnDef="x" or remove x from displayedColumns.

Property 'x' does not exist
-> Add x to model interface OR use correct field name.

Cannot find name 'MatInputModule'
-> Import it and add to component imports.

API 404
-> URL mismatch between frontend API service and backend controller.
```

Backend:

```txt
cannot find symbol
-> Class/method/import name wrong.

No property 'x' found for type
-> Repository method field name does not match entity field.

Validation failed
-> Required DTO field missing from frontend payload.

DataIntegrityViolationException
-> Duplicate unique field or FK child record blocking delete.

403 Forbidden
-> @PreAuthorize role mismatch.
```

## 53. Module File Map

Students:

```txt
Backend: Student.java, StudentDTO.java, StudentRepository.java, StudentService.java, StudentServiceImpl.java, StudentController.java, StudentMapper.java
Frontend: features/students/models/student.model.ts, services/student-api.service.ts, pages/student-*.component.*
```

Staff:

```txt
Backend: Staff.java, StaffDTO.java, StaffRepository.java, StaffService.java, StaffServiceImpl.java, StaffController.java, StaffMapper.java
Frontend: features/staff/models/staff.model.ts, services/staff-api.service.ts, pages/staff-*.component.*
```

Classes:

```txt
Backend: Class.java, ClassDTO.java, ClassRepository.java, ClassServiceImpl.java, ClassController.java, ClassMapper.java
Frontend: features/classes/*
```

Subjects:

```txt
Backend: Subject.java, SubjectDTO.java, SubjectRepository.java, SubjectServiceImpl.java, SubjectController.java, SubjectMapper.java
Frontend: features/subjects/*
```

Grades:

```txt
Backend: Grade.java, GradeDTO.java, GradeRepository.java, GradeServiceImpl.java, GradeController.java
Frontend: features/grades/*
```

Attendance:

```txt
Backend: Attendance.java, AttendanceDTO.java, BulkAttendanceRequest.java, AttendanceRepository.java, AttendanceServiceImpl.java, AttendanceController.java, AttendanceMapper.java
Frontend: features/attendance/*
```

Timetable:

```txt
Backend: Timetable.java, TimetableDTO.java, TimetableRepository.java, TimetableServiceImpl.java, TimetableController.java, TimetableMapper.java, SchoolDayPolicyServiceImpl.java
Frontend: features/timetable/*
```

Exams and marks:

```txt
Backend: Exam.java, StudentMark.java, ExamDTO.java, StudentMarkDTO.java, ExamRepository.java, StudentMarkRepository.java, ExamServiceImpl.java, StudentMarkServiceImpl.java, ExamController.java, StudentMarkController.java
Frontend: features/exams/*, features/student-marks/*
```

Parents and parent portal:

```txt
Backend: Parent.java, ParentStudent.java, ParentDTO.java, ParentPortal*DTO.java, ParentServiceImpl.java, ParentPortalServiceImpl.java, ParentController.java, ParentPortalController.java
Frontend: features/parents/*, features/parent-portal/*
```

Teacher portal and leave:

```txt
Backend: TeacherPortalController.java, TeacherPortalServiceImpl.java, TeacherLeaveController.java, TeacherLeaveServiceImpl.java, TeacherLeaveRequest.java, TeacherLeaveSession.java
Frontend: features/teacher-portal/*, features/teacher-leave/*
```

Documents and reports:

```txt
Backend: Document.java, DocumentDTO.java, DocumentServiceImpl.java, DocumentController.java, AcademicReportServiceImpl.java, AcademicReportPdfServiceImpl.java, AcademicReportController.java
Frontend: features/documents/*, features/reports/*
```

System/auth/users:

```txt
Backend: User.java, AuthController.java, AuthServiceImpl.java, UserServiceImpl.java, UserController.java, SecurityConfig.java, JwtService.java, SystemSettingsServiceImpl.java, SystemSettingsController.java
Frontend: features/auth/*, features/users/*, features/system-settings/*, core/services/auth.service.ts, core/interceptors/auth.interceptor.ts
```

## 54. Best Copy Sources In This Project

Simple CRUD copy:

```txt
features/grades/*
features/subjects/*
GradeController / GradeServiceImpl
SubjectController / SubjectServiceImpl
```

Filter copy:

```txt
StaffController / StaffServiceImpl / StaffRepository
features/staff/pages/staff-list-page.component.*
features/exams/pages/exam-list-page.component.*
features/attendance/pages/attendance-list-page.component.*
```

Form copy:

```txt
features/staff/pages/staff-form-page.component.*
features/students/pages/student-form-page.component.*
features/timetable/pages/timetable-form-page.component.*
```

Portal copy:

```txt
ParentPortalServiceImpl / ParentPortalController / features/parent-portal/*
TeacherPortalServiceImpl / TeacherPortalController / features/teacher-portal/*
```

Status approval copy:

```txt
LeaveRequestServiceImpl
TeacherLeaveServiceImpl
features/leave-requests/*
features/teacher-leave/*
```

## 55. Viva Modification Checklist

Before editing:

```txt
1. Is this frontend-only, backend-only, or both?
2. Which module?
3. Is it field, filter, validation, status, role, delete, or new page?
4. Find similar existing code.
5. Copy smallest matching method.
6. Rename carefully.
```

After editing:

```txt
1. Check imports.
2. Check API URL.
3. Check formControlName exists in TS.
4. Check displayedColumns match matColumnDef.
5. Check backend DTO field names match frontend model/payload.
6. Run npm run build.
7. Run mvn test or relevant test.
```

## 56. Quick Answer To Examiner

Use this sentence:

```txt
Sir, this change affects backend DTO/entity/service/controller and frontend model/API/component template. I will copy the same pattern from an existing module, rename the entity and field names, then run build/tests to catch mismatches.
```

For API:

```txt
Sir, I will add the endpoint in controller, declare it in service, implement business logic in service impl, add repository query if data filtering is needed, then call it from Angular API service and update the page component.
```

For UI:

```txt
Sir, this is frontend-only. I will update the component TS state/form, template binding, and SCSS layout. If it needs backend data, I will add the API service method too.
```
