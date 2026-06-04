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
import org.edu.dto.ExamDTO;
import org.edu.entity.AcademicTerm;
import org.edu.entity.AcademicYear;
import org.edu.entity.Exam;
import org.edu.entity.Subject;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.ExamMapper;
import org.edu.repository.AcademicTermRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.ExamRepository;
import org.edu.repository.SubjectRepository;
import org.edu.util.ExamType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {

    @Mock
    private ExamRepository examRepository;

    @Mock
    private AcademicYearRepository academicYearRepository;

    @Mock
    private AcademicTermRepository academicTermRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private SubjectRepository subjectRepository;

    private ExamServiceImpl examService;

    @BeforeEach
    void setUp() {
        ExamMapper examMapper = Mappers.getMapper(ExamMapper.class);
        examService = new ExamServiceImpl(
                examRepository,
                academicYearRepository,
                academicTermRepository,
                classRepository,
                subjectRepository,
                examMapper
        );
    }

    @Test
    void shouldCreateExamWhenAcademicRelationsAreValid() {
        ExamDTO dto = examRequest();
        AcademicYear year = academicYear();
        AcademicTerm term = academicTerm(year);
        org.edu.entity.Class studentClass = studentClass();
        Subject subject = subject();

        when(examRepository.existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCase(
                2L, 3L, 4L, "Term Test 1")).thenReturn(false);
        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(year));
        when(academicTermRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(term));
        when(classRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(studentClass));
        when(subjectRepository.findById(4L)).thenReturn(Optional.of(subject));
        when(examRepository.save(org.mockito.Mockito.any(Exam.class)))
                .thenAnswer(invocation -> {
                    Exam exam = invocation.getArgument(0);
                    exam.setId(9L);
                    return exam;
                });

        ExamDTO saved = examService.createExam(dto);

        assertEquals(9L, saved.getId());
        assertEquals("2026", saved.getAcademicYearName());
        assertEquals("Grade 10A", saved.getClassName());
        assertTrue(saved.isActive());
    }

    @Test
    void shouldRejectExamDateOutsideAcademicTerm() {
        ExamDTO dto = examRequest();
        dto.setExamDate(LocalDate.of(2026, 8, 1));
        AcademicYear year = academicYear();
        AcademicTerm term = academicTerm(year);

        when(examRepository.existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCase(
                2L, 3L, 4L, "Term Test 1")).thenReturn(false);
        when(academicYearRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(year));
        when(academicTermRepository.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(term));
        when(classRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(studentClass()));
        when(subjectRepository.findById(4L)).thenReturn(Optional.of(subject()));

        assertThrows(IllegalArgumentException.class, () -> examService.createExam(dto));
        verify(examRepository, never()).save(org.mockito.Mockito.any(Exam.class));
    }

    @Test
    void shouldRejectPassMarksGreaterThanMaxMarks() {
        ExamDTO dto = examRequest();
        dto.setMaxMarks(BigDecimal.valueOf(50));
        dto.setPassMarks(BigDecimal.valueOf(60));

        assertThrows(IllegalArgumentException.class, () -> examService.createExam(dto));
        verify(examRepository, never()).save(org.mockito.Mockito.any(Exam.class));
    }

    @Test
    void shouldRejectDuplicateExamInSameTermClassSubject() {
        ExamDTO dto = examRequest();

        when(examRepository.existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCase(
                2L, 3L, 4L, "Term Test 1")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> examService.createExam(dto));
        verify(examRepository, never()).save(org.mockito.Mockito.any(Exam.class));
    }

    @Test
    void shouldSoftDeactivateActiveExam() {
        Exam exam = new Exam();
        exam.setId(9L);
        exam.setActive(true);

        when(examRepository.findById(9L)).thenReturn(Optional.of(exam));

        examService.deactivateExam(9L);

        assertFalse(exam.isActive());
    }

    @Test
    void shouldRejectUpdateForInactiveExam() {
        when(examRepository.findByIdAndActiveTrue(9L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> examService.updateExam(9L, examRequest()));
    }

    private ExamDTO examRequest() {
        ExamDTO dto = new ExamDTO();
        dto.setAcademicYearId(1L);
        dto.setAcademicTermId(2L);
        dto.setClassId(3L);
        dto.setSubjectId(4L);
        dto.setName("Term Test 1");
        dto.setType(ExamType.TERM_TEST);
        dto.setExamDate(LocalDate.of(2026, 3, 15));
        dto.setMaxMarks(BigDecimal.valueOf(100));
        dto.setPassMarks(BigDecimal.valueOf(40));
        dto.setDescription("First term test");
        return dto;
    }

    private AcademicYear academicYear() {
        AcademicYear year = new AcademicYear();
        year.setId(1L);
        year.setName("2026");
        year.setStartDate(LocalDate.of(2026, 1, 1));
        year.setEndDate(LocalDate.of(2026, 12, 31));
        year.setActive(true);
        return year;
    }

    private AcademicTerm academicTerm(AcademicYear year) {
        AcademicTerm term = new AcademicTerm();
        term.setId(2L);
        term.setAcademicYear(year);
        term.setName("Term 1");
        term.setStartDate(LocalDate.of(2026, 1, 1));
        term.setEndDate(LocalDate.of(2026, 4, 30));
        term.setActive(true);
        return term;
    }

    private org.edu.entity.Class studentClass() {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(3L);
        studentClass.setName("Grade 10A");
        studentClass.setActive(true);
        return studentClass;
    }

    private Subject subject() {
        Subject subject = new Subject();
        subject.setId(4L);
        subject.setCode("MATH");
        subject.setName("Mathematics");
        return subject;
    }
}
