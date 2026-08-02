package org.edu.config;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.dto.AcademicReportDTO;
import org.edu.dto.request.AcademicReportGenerateRequest;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Attendance;
import org.edu.entity.Class;
import org.edu.entity.Document;
import org.edu.entity.Exam;
import org.edu.entity.Grade;
import org.edu.entity.LeaveRequest;
import org.edu.entity.Notice;
import org.edu.entity.Parent;
import org.edu.entity.ParentStudent;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.StudentMark;
import org.edu.entity.Subject;
import org.edu.entity.Timetable;
import org.edu.entity.User;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.AttendanceRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.DocumentRepository;
import org.edu.repository.ExamRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.LeaveRequestRepository;
import org.edu.repository.NoticeRepository;
import org.edu.repository.ParentRepository;
import org.edu.repository.ParentStudentRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentMarkRepository;
import org.edu.repository.StudentRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TimetableRepository;
import org.edu.repository.UserRepository;
import org.edu.service.AcademicReportService;
import org.edu.util.AttendanceStatus;
import org.edu.util.DocumentType;
import org.edu.util.ExamType;
import org.edu.util.LeaveRequestStatus;
import org.edu.util.NoticeAudience;
import org.edu.util.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("!test")
@Transactional
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Demo@1234";
    private static final String DEMO_ADMIN_EMAIL = "kasun.senanayake.demo@sms.lk";

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final ClassRepository classRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final TimetableRepository timetableRepository;
    private final ExamRepository examRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final AttendanceRepository attendanceRepository;
    private final NoticeRepository noticeRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final DocumentRepository documentRepository;
    private final AcademicReportService academicReportService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.documents.storage-dir:uploads/documents}")
    private String storageDir;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_ADMIN_EMAIL)) {
            log.info("Demo data already exists. Skipping seed run.");
            return;
        }

        log.info("Seeding realistic Sri Lankan demo data into the local database...");

        User adminUser = createUser("Kasun Senanayake", DEMO_ADMIN_EMAIL, Role.ADMIN);

        Staff nimal = createStaff(
                createUser("Anuradha Jayalath", "anuradha.jayalath.demo@sms.lk", Role.TEACHER),
                "DMT01",
                "Anuradha Jayalath",
                "0711234567",
                "Mathematics Teacher"
        );
        Staff saman = createStaff(
                createUser("Thushari Madugalle", "thushari.madugalle.demo@sms.lk", Role.TEACHER),
                "DMT02",
                "Thushari Madugalle",
                "0711234568",
                "Science Teacher"
        );
        Staff dinithi = createStaff(
                createUser("Ramesh Nawarathna", "ramesh.nawarathna.demo@sms.lk", Role.TEACHER),
                "DMT03",
                "Ramesh Nawarathna",
                "0711234569",
                "English Teacher"
        );
        Staff ravindu = createStaff(
                createUser("Shalani Senaviratne", "shalani.senaviratne.demo@sms.lk", Role.TEACHER),
                "DMT04",
                "Shalani Senaviratne",
                "0712234567",
                "ICT Teacher"
        );
        Staff chathuri = createStaff(
                createUser("Gayan Dissanayake", "gayan.dissanayake.demo@sms.lk", Role.TEACHER),
                "DMT05",
                "Gayan Dissanayake",
                "0712234568",
                "Sinhala Teacher"
        );

        Subject mathematics = createSubject("DMTH", "Mathematics", "Core mathematics syllabus for primary level");
        Subject science = createSubject("DSCI", "Science", "General science with theory and practical foundations");
        Subject english = createSubject("DENG", "English", "English language and literature studies");
        Subject ict = createSubject("DICT", "Information Technology", "Computer literacy, applications, and digital skills");
        Subject sinhala = createSubject("DSIN", "Sinhala", "Sinhala language, grammar, and comprehension");

        AcademicYear academicYear = createAcademicYear("2026 Academic Year", LocalDate.of(2026, 1, 6), LocalDate.of(2026, 12, 18));
        AcademicTerm termOne = createAcademicTerm(academicYear, "Term 1", LocalDate.of(2026, 1, 6), LocalDate.of(2026, 4, 10));

        Grade grade4 = createGrade("Grade 4", 4);
        Grade grade5 = createGrade("Grade 5", 5);
        Class grade4A = createClass(grade4, "Demo A", nimal, List.of(mathematics, science, english, ict, sinhala));
        Class grade5A = createClass(grade5, "Demo A", saman, List.of(mathematics, science, english, ict, sinhala));

        Map<String, Timetable> timetableMap = new LinkedHashMap<>();
        createTimetableEntries(grade4A, nimal, saman, dinithi, ravindu, chathuri, timetableMap);
        createTimetableEntries(grade5A, nimal, saman, dinithi, ravindu, chathuri, timetableMap);

        Student saduni = createStudent("Saduni Perera", LocalDate.of(2017, 3, 14), "Blue", grade4A);
        Student lakshan = createStudent("Lakshan Silva", LocalDate.of(2017, 7, 2), "Green", grade4A);
        Student tharushi = createStudent("Tharushi Nethmini", LocalDate.of(2017, 11, 8), "Red", grade4A);
        Student kavindu = createStudent("Kavindu Madushan", LocalDate.of(2017, 1, 19), "Yellow", grade4A);
        Student anudi = createStudent("Anudi Jayasekara", LocalDate.of(2016, 4, 28), "Blue", grade5A);
        Student dhanuka = createStudent("Dhanuka Ekanayake", LocalDate.of(2016, 9, 12), "Green", grade5A);
        Student malsha = createStudent("Malsha Fernando", LocalDate.of(2016, 6, 7), "Red", grade5A);
        Student vihanga = createStudent("Vihanga Gunasekara", LocalDate.of(2016, 12, 4), "Yellow", grade5A);

        Parent sunil = createParent(createUser("Sunil Perera", "sunil.perera.demo@sms.lk", Role.PARENT), "Sunil Perera", "0779876501", "12 Temple Road, Maharagama", "Bank Officer");
        Parent kumari = createParent(createUser("Kumari Silva", "kumari.silva.demo@sms.lk", Role.PARENT), "Kumari Silva", "0779876502", "45 Lake Drive, Piliyandala", "Teacher");
        Parent chamara = createParent(createUser("Chamara Neththikumara", "chamara.neththikumara.demo@sms.lk", Role.PARENT), "Chamara Neththikumara", "0779876503", "7 Station Road, Gampaha", "Small Business Owner");
        Parent deepika = createParent(createUser("Deepika Madushani", "deepika.madushani.demo@sms.lk", Role.PARENT), "Deepika Madushani", "0779876504", "28 Hospital Lane, Negombo", "Nurse");
        Parent ruwan = createParent(createUser("Ruwan Jayasekara", "ruwan.jayasekara.demo@sms.lk", Role.PARENT), "Ruwan Jayasekara", "0779876505", "61 Main Street, Kandy", "Accountant");
        Parent nilmini = createParent(createUser("Nilmini Ekanayake", "nilmini.ekanayake.demo@sms.lk", Role.PARENT), "Nilmini Ekanayake", "0779876506", "33 School Lane, Matale", "Government Clerk");
        Parent priyantha = createParent(createUser("Priyantha Fernando", "priyantha.fernando.demo@sms.lk", Role.PARENT), "Priyantha Fernando", "0779876507", "90 Temple Street, Kurunegala", "Retail Manager");

        linkParentToStudent(sunil, saduni, "Father", true, true);
        linkParentToStudent(kumari, lakshan, "Mother", true, true);
        linkParentToStudent(chamara, tharushi, "Father", true, true);
        linkParentToStudent(deepika, kavindu, "Mother", true, true);
        linkParentToStudent(ruwan, anudi, "Father", true, true);
        linkParentToStudent(nilmini, dhanuka, "Mother", true, true);
        linkParentToStudent(priyantha, malsha, "Father", true, true);
        linkParentToStudent(priyantha, vihanga, "Father", true, true);

        List<Exam> exams = createExams(termOne, academicYear, grade4A, grade5A, mathematics, science, english, ict, sinhala);

        createMarksForGrade4(exams, nimal, dinithi, ravindu, chathuri, saman, saduni, lakshan, tharushi, kavindu);
        createMarksForGrade5(exams, nimal, dinithi, ravindu, chathuri, saman, anudi, dhanuka, malsha, vihanga);

        seedAttendance(grade4A, List.of(saduni, lakshan, tharushi, kavindu), timetableMap);
        seedAttendance(grade5A, List.of(anudi, dhanuka, malsha, vihanga), timetableMap);

        createNotice("School Reopening for 2026", "The first school term begins on 6 January 2026. Students should report by 7.20 a.m. in full uniform.", NoticeAudience.ALL, null, true, LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 20));
        createNotice("Parent-Teacher Meeting", "Parents are invited for the Term 1 parent-teacher meeting on 15 February 2026 from 8.30 a.m. to 1.00 p.m.", NoticeAudience.PARENTS, null, true, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 16));
        createNotice("Grade 4 Demo A Science Activity Day", "Grade 4 Demo A students should bring chart paper for the science activity day next Wednesday.", NoticeAudience.CLASS, grade4A, true, LocalDate.of(2026, 2, 8), LocalDate.of(2026, 2, 20));
        createNotice("Grade 5 Demo A ICT Session", "Grade 5 Demo A students will attend the ICT skills session in the auditorium after interval.", NoticeAudience.CLASS, grade5A, true, LocalDate.of(2026, 3, 3), LocalDate.of(2026, 3, 10));

        createLeaveRequest(sunil, saduni, LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 12), "Fever and rest advised by doctor", "Medical certificate submitted to class teacher", LeaveRequestStatus.APPROVED, nimal, "Approved with medical proof", true);
        createLeaveRequest(ruwan, anudi, LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 5), "Family religious observance", "Student will collect missed notes the next day", LeaveRequestStatus.PENDING, null, null, false);
        createLeaveRequest(priyantha, vihanga, LocalDate.of(2026, 2, 24), LocalDate.of(2026, 2, 24), "Travel request without supporting documents", "Requested at short notice", LeaveRequestStatus.REJECTED, saman, "Please submit supporting evidence earlier", false);
        createLeaveRequest(nilmini, dhanuka, LocalDate.of(2026, 3, 18), LocalDate.of(2026, 3, 19), "Family event out of town", "Cancelled after travel plans changed", LeaveRequestStatus.CANCELLED, null, "Cancelled by parent", false);

        createDocument(saduni, nimal.getUser(), DocumentType.MEDICAL_RECORD, "Saduni medical note", "Medical note for February absence", "saduni-medical-note.txt", "text/plain", "Patient advised to rest for two days due to viral fever.\nIssued by Family Clinic, Maharagama.", true);
        createDocument(anudi, saman.getUser(), DocumentType.STUDENT_RECORD, "Anudi progress note", "Term 1 progress observation by class teacher", "anudi-progress-note.txt", "text/plain", "Anudi is punctual, active in class discussions, and shows strong English presentation skills.", true);
        createDocument(vihanga, saman.getUser(), DocumentType.LEAVE_LETTER, "Vihanga leave request copy", "Archived leave letter retained for class teacher follow-up", "vihanga-leave-letter.txt", "text/plain", "Leave request received without supporting documents. Follow-up required.", false);

        generateAcademicReports(adminUser, termOne, List.of(saduni, lakshan, tharushi, kavindu, anudi, dhanuka, malsha, vihanga));

        log.info("Demo data seeded successfully.");
        log.info("Demo login password for all seeded users: {}", DEMO_PASSWORD);
        log.info("Seed summary: users={}, staff={}, parents={}, students={}, classes={}, subjects={}, exams={}, marks={}, attendance={}, notices={}, leaveRequests={}, documents={}",
                userRepository.count(),
                staffRepository.count(),
                parentRepository.count(),
                studentRepository.count(),
                classRepository.count(),
                subjectRepository.count(),
                examRepository.count(),
                studentMarkRepository.count(),
                attendanceRepository.count(),
                noticeRepository.count(),
                leaveRequestRepository.count(),
                documentRepository.count());
    }

    private User createUser(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        user.setRole(role);
        return userRepository.save(user);
    }

    private Staff createStaff(User user, String staffId, String name, String phoneNumber, String designation) {
        Staff staff = new Staff();
        staff.setUser(user);
        staff.setStaffId(staffId);
        staff.setName(name);
        staff.setPhoneNumber(phoneNumber);
        staff.setDesignation(designation);
        staff.setActive(true);
        return staffRepository.save(staff);
    }

    private Parent createParent(User user, String name, String phoneNumber, String address, String occupation) {
        Parent parent = new Parent();
        parent.setUser(user);
        parent.setName(name);
        parent.setPhoneNumber(phoneNumber);
        parent.setAddress(address);
        parent.setOccupation(occupation);
        parent.setActive(true);
        return parentRepository.save(parent);
    }

    private Student createStudent(String name, LocalDate dateOfBirth, String house, Class studentClass) {
        Student student = new Student();
        student.setName(name);
        student.setDateOfBirth(dateOfBirth);
        student.setHouse(house);
        student.setCurrentClass(studentClass);
        student.setActive(true);
        return studentRepository.save(student);
    }

    private void linkParentToStudent(Parent parent, Student student, String relationshipType, boolean primary, boolean emergency) {
        ParentStudent link = new ParentStudent();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType(relationshipType);
        link.setPrimaryContact(primary);
        link.setEmergencyContact(emergency);
        parentStudentRepository.save(link);
    }

    private Subject createSubject(String code, String name, String description) {
        Subject subject = new Subject();
        subject.setCode(code);
        subject.setName(name);
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

    private AcademicYear createAcademicYear(String name, LocalDate startDate, LocalDate endDate) {
        AcademicYear year = new AcademicYear();
        year.setName(name);
        year.setStartDate(startDate);
        year.setEndDate(endDate);
        year.setCurrent(true);
        year.setActive(true);
        return academicYearRepository.save(year);
    }

    private AcademicTerm createAcademicTerm(AcademicYear year, String name, LocalDate startDate, LocalDate endDate) {
        AcademicTerm term = new AcademicTerm();
        term.setAcademicYear(year);
        term.setName(name);
        term.setStartDate(startDate);
        term.setEndDate(endDate);
        term.setCurrent(true);
        term.setActive(true);
        return academicTermRepository.save(term);
    }

    private Grade createGrade(String name, int level) {
        return gradeRepository.findByLevel(level).orElseGet(() -> {
            Grade grade = new Grade();
            grade.setName(name);
            grade.setLevel(level);
            grade.setActive(true);
            return gradeRepository.save(grade);
        });
    }

    private Class createClass(Grade grade, String section, Staff classTeacher, List<Subject> subjects) {
        Class studentClass = new Class();
        studentClass.setGrade(grade);
        studentClass.setSection(section.trim().toUpperCase());
        studentClass.setName(grade.getName() + " " + section);
        studentClass.setClassTeacher(classTeacher);
        studentClass.setSubjects(new ArrayList<>(subjects));
        studentClass.setActive(true);
        return classRepository.save(studentClass);
    }

    private void createTimetableEntries(
            Class studentClass,
            Staff mathematicsTeacher,
            Staff scienceTeacher,
            Staff englishTeacher,
            Staff ictTeacher,
            Staff sinhalaTeacher,
            Map<String, Timetable> timetableMap
    ) {
        createTimetable(studentClass, "DMTH", mathematicsTeacher, DayOfWeek.MONDAY, LocalTime.of(7, 30), LocalTime.of(8, 30), "Room " + studentClass.getName(), timetableMap);
        createTimetable(studentClass, "DSCI", scienceTeacher, DayOfWeek.TUESDAY, LocalTime.of(7, 30), LocalTime.of(8, 30), "Science Lab", timetableMap);
        createTimetable(studentClass, "DENG", englishTeacher, DayOfWeek.WEDNESDAY, LocalTime.of(7, 30), LocalTime.of(8, 30), "Room " + studentClass.getName(), timetableMap);
        createTimetable(studentClass, "DICT", ictTeacher, DayOfWeek.THURSDAY, LocalTime.of(7, 30), LocalTime.of(8, 30), "ICT Lab", timetableMap);
        createTimetable(studentClass, "DSIN", sinhalaTeacher, DayOfWeek.FRIDAY, LocalTime.of(7, 30), LocalTime.of(8, 30), "Room " + studentClass.getName(), timetableMap);
    }

    private void createTimetable(
            Class studentClass,
            String subjectCode,
            Staff teacher,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            String room,
            Map<String, Timetable> timetableMap
    ) {
        Subject subject = subjectRepository.findByCode(subjectCode).orElseThrow();
        Timetable timetable = new Timetable();
        timetable.setStudentClass(studentClass);
        timetable.setSubject(subject);
        timetable.setStaff(teacher);
        timetable.setDayOfWeek(dayOfWeek);
        timetable.setStartTime(startTime);
        timetable.setEndTime(endTime);
        timetable.setRoomNumber(room);
        timetable = timetableRepository.save(timetable);
        timetableMap.put(studentClass.getName() + "-" + subjectCode, timetable);
    }

    private List<Exam> createExams(
            AcademicTerm term,
            AcademicYear year,
            Class grade4A,
            Class grade5A,
            Subject mathematics,
            Subject science,
            Subject english,
            Subject ict,
            Subject sinhala
    ) {
        List<Exam> exams = new ArrayList<>();
        exams.add(createExam(year, term, grade4A, mathematics, "Grade 4 Demo A Term 1 Mathematics", LocalDate.of(2026, 2, 10), "Term 1 mathematics paper"));
        exams.add(createExam(year, term, grade4A, science, "Grade 4 Demo A Term 1 Science", LocalDate.of(2026, 2, 12), "Term 1 integrated science paper"));
        exams.add(createExam(year, term, grade4A, english, "Grade 4 Demo A Term 1 English", LocalDate.of(2026, 2, 16), "Term 1 English language paper"));
        exams.add(createExam(year, term, grade4A, ict, "Grade 4 Demo A Term 1 ICT", LocalDate.of(2026, 2, 18), "Term 1 ICT theory test"));
        exams.add(createExam(year, term, grade4A, sinhala, "Grade 4 Demo A Term 1 Sinhala", LocalDate.of(2026, 2, 20), "Term 1 Sinhala paper"));
        exams.add(createExam(year, term, grade5A, mathematics, "Grade 5 Demo A Term 1 Mathematics", LocalDate.of(2026, 2, 11), "Term 1 mathematics paper"));
        exams.add(createExam(year, term, grade5A, science, "Grade 5 Demo A Term 1 Science", LocalDate.of(2026, 2, 13), "Term 1 integrated science paper"));
        exams.add(createExam(year, term, grade5A, english, "Grade 5 Demo A Term 1 English", LocalDate.of(2026, 2, 17), "Term 1 English language paper"));
        exams.add(createExam(year, term, grade5A, ict, "Grade 5 Demo A Term 1 ICT", LocalDate.of(2026, 2, 19), "Term 1 ICT theory test"));
        exams.add(createExam(year, term, grade5A, sinhala, "Grade 5 Demo A Term 1 Sinhala", LocalDate.of(2026, 2, 23), "Term 1 Sinhala paper"));
        return exams;
    }

    private Exam createExam(AcademicYear year, AcademicTerm term, Class studentClass, Subject subject, String name, LocalDate examDate, String description) {
        Exam exam = new Exam();
        exam.setAcademicYear(year);
        exam.setAcademicTerm(term);
        exam.setStudentClass(studentClass);
        exam.setSubject(subject);
        exam.setName(name);
        exam.setType(ExamType.TERM_TEST);
        exam.setExamDate(examDate);
        exam.setMaxMarks(BigDecimal.valueOf(100));
        exam.setPassMarks(BigDecimal.valueOf(40));
        exam.setActive(true);
        exam.setDescription(description);
        return examRepository.save(exam);
    }

    private void createMarksForGrade4(
            List<Exam> exams,
            Staff mathematicsTeacher,
            Staff englishTeacher,
            Staff ictTeacher,
            Staff sinhalaTeacher,
            Staff scienceTeacher,
            Student saduni,
            Student lakshan,
            Student tharushi,
            Student kavindu
    ) {
        Map<String, Integer> saduniMarks = Map.of("DMTH", 86, "DSCI", 79, "DENG", 88, "DICT", 91, "DSIN", 77);
        Map<String, Integer> lakshanMarks = Map.of("DMTH", 68, "DSCI", 72, "DENG", 61, "DICT", 74, "DSIN", 66);
        Map<String, Integer> tharushiMarks = Map.of("DMTH", 75, "DSCI", 81, "DENG", 83, "DICT", 78, "DSIN", 80);
        Map<String, Integer> kavinduMarks = Map.of("DMTH", 44, "DSCI", 58, "DENG", 49, "DICT", 63, "DSIN", 52);
        seedClassMarks(exams.subList(0, 5), List.of(
                studentScore(saduni, saduniMarks),
                studentScore(lakshan, lakshanMarks),
                studentScore(tharushi, tharushiMarks),
                studentScore(kavindu, kavinduMarks)
        ), Map.of("DMTH", mathematicsTeacher, "DSCI", scienceTeacher, "DENG", englishTeacher, "DICT", ictTeacher, "DSIN", sinhalaTeacher));
    }

    private void createMarksForGrade5(
            List<Exam> exams,
            Staff mathematicsTeacher,
            Staff englishTeacher,
            Staff ictTeacher,
            Staff sinhalaTeacher,
            Staff scienceTeacher,
            Student anudi,
            Student dhanuka,
            Student malsha,
            Student vihanga
    ) {
        Map<String, Integer> anudiMarks = Map.of("DMTH", 92, "DSCI", 87, "DENG", 90, "DICT", 89, "DSIN", 74);
        Map<String, Integer> dhanukaMarks = Map.of("DMTH", 59, "DSCI", 64, "DENG", 57, "DICT", 71, "DSIN", 60);
        Map<String, Integer> malshaMarks = Map.of("DMTH", 81, "DSCI", 77, "DENG", 84, "DICT", 86, "DSIN", 79);
        Map<String, Integer> vihangaMarks = Map.of("DMTH", 38, "DSCI", 47, "DENG", 42, "DICT", 55, "DSIN", 40);
        seedClassMarks(exams.subList(5, 10), List.of(
                studentScore(anudi, anudiMarks),
                studentScore(dhanuka, dhanukaMarks),
                studentScore(malsha, malshaMarks),
                studentScore(vihanga, vihangaMarks)
        ), Map.of("DMTH", mathematicsTeacher, "DSCI", scienceTeacher, "DENG", englishTeacher, "DICT", ictTeacher, "DSIN", sinhalaTeacher));
    }

    private Map.Entry<Student, Map<String, Integer>> studentScore(Student student, Map<String, Integer> marks) {
        return Map.entry(student, marks);
    }

    private void seedClassMarks(
            List<Exam> classExams,
            List<Map.Entry<Student, Map<String, Integer>>> studentMarks,
            Map<String, Staff> teacherBySubject
    ) {
        for (Exam exam : classExams) {
            String subjectCode = exam.getSubject().getCode();
            for (Map.Entry<Student, Map<String, Integer>> entry : studentMarks) {
                int score = entry.getValue().get(subjectCode);
                createStudentMark(exam, entry.getKey(), teacherBySubject.get(subjectCode), BigDecimal.valueOf(score), "Seeded demo mark");
            }
        }
    }

    private void createStudentMark(Exam exam, Student student, Staff enteredBy, BigDecimal marksObtained, String remarks) {
        StudentMark studentMark = new StudentMark();
        studentMark.setExam(exam);
        studentMark.setStudent(student);
        studentMark.setEnteredBy(enteredBy);
        studentMark.setMarksObtained(marksObtained.setScale(2, RoundingMode.HALF_UP));
        studentMark.setPercentage(marksObtained.multiply(BigDecimal.valueOf(100)).divide(exam.getMaxMarks(), 2, RoundingMode.HALF_UP));
        studentMark.setGrade(calculateGrade(studentMark.getPercentage()));
        studentMark.setPassed(marksObtained.compareTo(exam.getPassMarks()) >= 0);
        studentMark.setRemarks(remarks);
        studentMarkRepository.save(studentMark);
    }

    private void seedAttendance(Class studentClass, List<Student> students, Map<String, Timetable> timetableMap) {
        List<LocalDate> dates = List.of(
                LocalDate.of(2026, 1, 12),
                LocalDate.of(2026, 1, 13),
                LocalDate.of(2026, 1, 14),
                LocalDate.of(2026, 2, 2),
                LocalDate.of(2026, 2, 3),
                LocalDate.of(2026, 2, 4)
        );
        List<Timetable> timetables = List.of(
                timetableMap.get(studentClass.getName() + "-DMTH"),
                timetableMap.get(studentClass.getName() + "-DSCI"),
                timetableMap.get(studentClass.getName() + "-DENG"),
                timetableMap.get(studentClass.getName() + "-DICT"),
                timetableMap.get(studentClass.getName() + "-DSIN"),
                timetableMap.get(studentClass.getName() + "-DMTH")
        );

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            for (int j = 0; j < dates.size(); j++) {
                AttendanceStatus status = attendanceStatusFor(i, j);
                createAttendance(student, studentClass, timetables.get(j), dates.get(j), status);
            }
        }
    }

    private AttendanceStatus attendanceStatusFor(int studentIndex, int slotIndex) {
        if (studentIndex == 0 && slotIndex == 2) {
            return AttendanceStatus.LATE;
        }
        if (studentIndex == 1 && slotIndex == 4) {
            return AttendanceStatus.ABSENT;
        }
        if (studentIndex == 2 && slotIndex == 1) {
            return AttendanceStatus.EXCUSED;
        }
        if (studentIndex == 3 && slotIndex == 0) {
            return AttendanceStatus.LATE;
        }
        return AttendanceStatus.PRESENT;
    }

    private void createAttendance(Student student, Class studentClass, Timetable timetable, LocalDate attendanceDate, AttendanceStatus status) {
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setStudentClass(studentClass);
        attendance.setSubject(timetable.getSubject());
        attendance.setTimetable(timetable);
        attendance.setMarkedBy(timetable.getStaff());
        attendance.setAttendanceDate(attendanceDate);
        attendance.setStatus(status);
        attendance.setRemarks(status == AttendanceStatus.PRESENT ? "Present for scheduled lesson" : "Seeded demo attendance status");
        attendanceRepository.save(attendance);
    }

    private void createNotice(
            String title,
            String message,
            NoticeAudience audience,
            Class targetClass,
            boolean published,
            LocalDate publishDate,
            LocalDate expiryDate
    ) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setMessage(message);
        notice.setAudience(audience);
        notice.setTargetClass(targetClass);
        notice.setPublished(published);
        notice.setPublishDate(publishDate);
        notice.setExpiryDate(expiryDate);
        notice.setActive(true);
        noticeRepository.save(notice);
    }

    private void createLeaveRequest(
            Parent parent,
            Student student,
            LocalDate startDate,
            LocalDate endDate,
            String reason,
            String note,
            LeaveRequestStatus status,
            Staff reviewedBy,
            String reviewerRemarks,
            boolean attendanceApplied
    ) {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setParent(parent);
        leaveRequest.setStudent(student);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
        leaveRequest.setReason(reason);
        leaveRequest.setNote(note);
        leaveRequest.setStatus(status);
        leaveRequest.setReviewedBy(reviewedBy);
        leaveRequest.setReviewerRemarks(reviewerRemarks);
        if (status != LeaveRequestStatus.PENDING) {
            leaveRequest.setReviewedAt(LocalDateTime.now().minusDays(60));
        }
        leaveRequest.setAttendanceApplied(attendanceApplied);
        if (attendanceApplied) {
            leaveRequest.setAttendanceAppliedAt(LocalDateTime.now().minusDays(59));
        }
        leaveRequestRepository.save(leaveRequest);
    }

    private void createDocument(
            Student student,
            User uploadedBy,
            DocumentType type,
            String title,
            String description,
            String originalFileName,
            String contentType,
            String fileContent,
            boolean visibleToParent
    ) {
        String storedFileName = "demo-" + student.getId() + "-" + originalFileName;
        Path storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(storageRoot);
            Files.writeString(storageRoot.resolve(storedFileName), fileContent, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to write seeded document file: " + originalFileName, ex);
        }

        Document document = new Document();
        document.setStudent(student);
        document.setUploadedBy(uploadedBy);
        document.setDocumentType(type);
        document.setTitle(title);
        document.setDescription(description);
        document.setOriginalFileName(originalFileName);
        document.setStoredFileName(storedFileName);
        document.setContentType(contentType);
        document.setFileSize(fileContent.getBytes(StandardCharsets.UTF_8).length);
        document.setVisibleToParent(visibleToParent);
        document.setActive(true);
        documentRepository.save(document);
    }

    private void generateAcademicReports(User adminUser, AcademicTerm term, List<Student> students) {
        for (Student student : students) {
            AcademicReportGenerateRequest request = new AcademicReportGenerateRequest();
            request.setStudentId(student.getId());
            request.setAcademicTermId(term.getId());
            request.setClassTeacherRemarks("Steady performance shown during the first term. Continue structured revision and classroom participation.");
            request.setPrincipalRemarks("Satisfactory progress for the term. Maintain discipline and regular attendance.");

            AcademicReportDTO report = academicReportService.generateReport(adminUser.getId(), request);
            academicReportService.publishReport(adminUser.getId(), report.getId());
        }
    }

    private String calculateGrade(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return "A";
        }
        if (percentage.compareTo(BigDecimal.valueOf(65)) >= 0) {
            return "B";
        }
        if (percentage.compareTo(BigDecimal.valueOf(55)) >= 0) {
            return "C";
        }
        if (percentage.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return "S";
        }
        return "F";
    }
}
