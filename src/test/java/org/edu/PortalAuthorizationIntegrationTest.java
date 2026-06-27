package org.edu;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.User;
import org.edu.repository.ClassRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.UserRepository;
import org.edu.security.JwtService;
import org.edu.util.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PortalAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ParentStudentRepository parentStudentRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void shouldRejectUnauthenticatedPortalAccess() throws Exception {
        mockMvc.perform(get("/api/parent-portal/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.message", is("Unauthorized access")))
                .andExpect(jsonPath("$.path", is("/api/parent-portal/profile")));
    }

    @Test
    void shouldRejectMalformedBearerToken() throws Exception {
        mockMvc.perform(get("/api/parent-portal/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED")))
                .andExpect(jsonPath("$.message", is("Unauthorized access")));
    }

    @Test
    void shouldEnforcePortalRoles() throws Exception {
        User parentUser = saveUser("parent-role@example.com", Role.PARENT);
        saveParent(parentUser, "Parent User");
        User teacherUser = saveUser("teacher-role@example.com", Role.TEACHER);
        saveTeacher(teacherUser, "T-ROLE");

        mockMvc.perform(get("/api/teacher-portal/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(parentUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")))
                .andExpect(jsonPath("$.message", is("Access denied")));

        mockMvc.perform(get("/api/parent-portal/profile")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")))
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    void shouldReturnOnlyAuthenticatedParentsLinkedStudents() throws Exception {
        User parentUser = saveUser("parent-linked@example.com", Role.PARENT);
        Parent parent = saveParent(parentUser, "Linked Parent");
        org.edu.entity.Class studentClass = saveClass("Grade 10A", null);
        Student linkedStudent = saveStudent("linked-student@example.com", "Linked Student", studentClass);
        saveStudent("unlinked-student@example.com", "Unlinked Student", studentClass);
        link(parent, linkedStudent);

        mockMvc.perform(get("/api/parent-portal/students")
                        .header(HttpHeaders.AUTHORIZATION, bearer(parentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].studentId", is(linkedStudent.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Linked Student")));
    }

    @Test
    void shouldRejectParentAccessToUnlinkedStudent() throws Exception {
        User parentUser = saveUser("parent-isolation@example.com", Role.PARENT);
        saveParent(parentUser, "Isolated Parent");
        org.edu.entity.Class studentClass = saveClass("Grade 9A", null);
        Student otherStudent = saveStudent("other-student@example.com", "Other Student", studentClass);

        mockMvc.perform(get("/api/parent-portal/students/{studentId}", otherStudent.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(parentUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Student not found in current parent portal")));
    }

    @Test
    void shouldAllowTeacherAccessOnlyToAssignedClasses() throws Exception {
        User teacherUser = saveUser("teacher-scope@example.com", Role.TEACHER);
        Staff teacher = saveTeacher(teacherUser, "T-SCOPE");
        org.edu.entity.Class assignedClass = saveClass("Grade 11A", teacher);
        org.edu.entity.Class otherClass = saveClass("Grade 11B", null);
        Student assignedStudent = saveStudent("assigned-student@example.com", "Assigned Student", assignedClass);
        saveStudent("other-class-student@example.com", "Other Class Student", otherClass);

        mockMvc.perform(get("/api/teacher-portal/classes/{classId}/students", assignedClass.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].studentId", is(assignedStudent.getId().intValue())));

        mockMvc.perform(get("/api/teacher-portal/classes/{classId}/students", otherClass.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Class not found in current teacher portal")));
    }

    @Test
    void shouldBlockTeacherFromAdminManagementApis() throws Exception {
        User teacherUser = saveUser("teacher-admin-block@example.com", Role.TEACHER);
        saveTeacher(teacherUser, "T-BLOCK");

        mockMvc.perform(get("/api/students")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));

        mockMvc.perform(get("/api/classes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("ACCESS_DENIED")));

        mockMvc.perform(get("/api/teacher-portal/academic-terms")
                        .header(HttpHeaders.AUTHORIZATION, bearer(teacherUser)))
                .andExpect(status().isOk());
    }

    private User saveUser(String email, Role role) {
        User user = new User();
        user.setName(role.name() + " User");
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setRole(role);
        return userRepository.save(user);
    }

    private Parent saveParent(User user, String name) {
        Parent parent = new Parent();
        parent.setUser(user);
        parent.setName(name);
        parent.setPhoneNumber("0771234567");
        parent.setAddress("Colombo");
        parent.setOccupation("Guardian");
        parent.setActive(true);
        return parentRepository.save(parent);
    }

    private Staff saveTeacher(User user, String staffCode) {
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setStaffId(staffCode);
        staff.setName("Teacher " + staffCode);
        staff.setPhoneNumber("0711234567");
        staff.setDesignation("Teacher");
        staff.setActive(true);
        return staffRepository.save(staff);
    }

    private org.edu.entity.Class saveClass(String name, Staff classTeacher) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setName(name);
        studentClass.setClassTeacher(classTeacher);
        studentClass.setActive(true);
        return classRepository.save(studentClass);
    }

    private Student saveStudent(String email, String name, org.edu.entity.Class studentClass) {
        Student student = new Student();
        student.setUser(saveUser(email, Role.STUDENT));
        student.setName(name);
        student.setDateOfBirth(LocalDate.of(2010, 1, 1));
        student.setCurrentClass(studentClass);
        student.setPhoneNumber("0751234567");
        student.setActive(true);
        return studentRepository.save(student);
    }

    private void link(Parent parent, Student student) {
        ParentStudent link = new ParentStudent();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType("Guardian");
        link.setPrimaryContact(true);
        link.setEmergencyContact(true);
        parentStudentRepository.save(link);
    }

    private String bearer(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
