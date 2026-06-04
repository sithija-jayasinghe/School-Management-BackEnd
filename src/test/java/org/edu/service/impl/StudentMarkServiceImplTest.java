package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.edu.dto.StudentMarkDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Exam;
import org.edu.entity.Student;
import org.edu.entity.StudentMark;
import org.edu.entity.Subject;
import org.edu.mapper.StudentMarkMapper;
import org.edu.repository.ExamRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentMarkRepository;
import org.edu.repository.StudentRepository;
import org.edu.util.ExamType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentMarkServiceImplTest {

    @Mock
    private StudentMarkRepository studentMarkRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StaffRepository staffRepository;

    private StudentMarkServiceImpl studentMarkService;

    @BeforeEach
    void setUp() {
        StudentMarkMapper studentMarkMapper = Mappers.getMapper(StudentMarkMapper.class);
        studentMarkService = new StudentMarkServiceImpl(
                studentMarkRepository,
                examRepository,
                studentRepository,
                staffRepository,
                studentMarkMapper
        );
    }

    @Test
    void shouldCreateStudentMarkWithCalculatedGradeAndPassStatus() {
        StudentMarkDTO dto = markRequest(BigDecimal.valueOf(82));
        Exam exam = exam();
        Student student = student(20L, 30L);

        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(studentMarkRepository.existsByExamIdAndStudentId(10L, 20L)).thenReturn(false);
        when(studentMarkRepository.save(org.mockito.Mockito.any(StudentMark.class)))
                .thenAnswer(invocation -> {
                    StudentMark mark = invocation.getArgument(0);
                    mark.setId(99L);
                    return mark;
                });

        StudentMarkDTO saved = studentMarkService.createStudentMark(dto);

        assertEquals(99L, saved.getId());
        assertEquals(BigDecimal.valueOf(82).setScale(2), saved.getPercentage());
        assertEquals("A", saved.getGrade());
        assertTrue(saved.isPassed());
    }

    @Test
    void shouldRejectMarksGreaterThanExamMaxMarks() {
        StudentMarkDTO dto = markRequest(BigDecimal.valueOf(120));
        Exam exam = exam();
        Student student = student(20L, 30L);

        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> studentMarkService.createStudentMark(dto));
        verify(studentMarkRepository, never()).save(org.mockito.Mockito.any(StudentMark.class));
    }

    @Test
    void shouldRejectDuplicateMarksForSameStudentAndExam() {
        StudentMarkDTO dto = markRequest(BigDecimal.valueOf(70));
        Exam exam = exam();
        Student student = student(20L, 30L);

        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(studentMarkRepository.existsByExamIdAndStudentId(10L, 20L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> studentMarkService.createStudentMark(dto));
        verify(studentMarkRepository, never()).save(org.mockito.Mockito.any(StudentMark.class));
    }

    @Test
    void shouldRejectStudentOutsideExamClass() {
        StudentMarkDTO dto = markRequest(BigDecimal.valueOf(70));
        Exam exam = exam();
        Student student = student(20L, 999L);

        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> studentMarkService.createStudentMark(dto));
    }

    @Test
    void shouldUpdateMarkAndRecalculateResult() {
        Exam exam = exam();
        Student student = student(20L, 30L);
        StudentMark existing = existingMark(exam, student, BigDecimal.valueOf(35));
        StudentMarkDTO dto = new StudentMarkDTO();
        dto.setMarksObtained(BigDecimal.valueOf(55));

        when(studentMarkRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(studentMarkRepository.existsByExamIdAndStudentIdAndIdNot(10L, 20L, 99L)).thenReturn(false);
        when(studentMarkRepository.save(existing)).thenReturn(existing);

        StudentMarkDTO updated = studentMarkService.updateStudentMark(99L, dto);

        assertEquals(BigDecimal.valueOf(55).setScale(2), updated.getPercentage());
        assertEquals("C", updated.getGrade());
        assertTrue(updated.isPassed());
    }

    @Test
    void shouldMarkFailedWhenBelowPassMarks() {
        StudentMarkDTO dto = markRequest(BigDecimal.valueOf(35));
        Exam exam = exam();
        Student student = student(20L, 30L);

        when(examRepository.findByIdAndActiveTrue(10L)).thenReturn(Optional.of(exam));
        when(studentRepository.findByIdAndActiveTrue(20L)).thenReturn(Optional.of(student));
        when(studentMarkRepository.existsByExamIdAndStudentId(10L, 20L)).thenReturn(false);
        when(studentMarkRepository.save(org.mockito.Mockito.any(StudentMark.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentMarkDTO saved = studentMarkService.createStudentMark(dto);

        assertEquals("F", saved.getGrade());
        assertFalse(saved.isPassed());
    }

    private StudentMarkDTO markRequest(BigDecimal marks) {
        StudentMarkDTO dto = new StudentMarkDTO();
        dto.setExamId(10L);
        dto.setStudentId(20L);
        dto.setMarksObtained(marks);
        dto.setRemarks("Good work");
        return dto;
    }

    private StudentMark existingMark(Exam exam, Student student, BigDecimal marks) {
        StudentMark studentMark = new StudentMark();
        studentMark.setId(99L);
        studentMark.setExam(exam);
        studentMark.setStudent(student);
        studentMark.setMarksObtained(marks);
        studentMark.setPercentage(marks);
        studentMark.setGrade("F");
        studentMark.setPassed(false);
        return studentMark;
    }

    private Exam exam() {
        Exam exam = new Exam();
        exam.setId(10L);
        exam.setName("Term Test 1");
        exam.setType(ExamType.TERM_TEST);
        exam.setExamDate(LocalDate.of(2026, 3, 15));
        exam.setAcademicYear(new AcademicYear());
        exam.setAcademicTerm(new AcademicTerm());
        exam.setStudentClass(studentClass(30L));
        exam.setSubject(subject());
        exam.setMaxMarks(BigDecimal.valueOf(100));
        exam.setPassMarks(BigDecimal.valueOf(40));
        exam.setActive(true);
        return exam;
    }

    private Student student(Long studentId, Long classId) {
        Student student = new Student();
        student.setId(studentId);
        student.setName("Student User");
        student.setActive(true);
        student.setCurrentClass(studentClass(classId));
        return student;
    }

    private org.edu.entity.Class studentClass(Long classId) {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(classId);
        studentClass.setName("Grade 10A");
        studentClass.setActive(true);
        return studentClass;
    }

    private Subject subject() {
        Subject subject = new Subject();
        subject.setId(40L);
        subject.setCode("MATH");
        subject.setName("Mathematics");
        return subject;
    }
}
