package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.edu.dto.SubjectDTO;
import org.edu.entity.Grade;
import org.edu.entity.Subject;
import org.edu.mapper.ClassMapper;
import org.edu.mapper.SubjectMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectServiceImplTest {

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private ClassRepository classRepository;

    @Mock
    private GradeRepository gradeRepository;

    private SubjectServiceImpl subjectService;

    @BeforeEach
    void setUp() {
        SubjectMapper subjectMapper = Mappers.getMapper(SubjectMapper.class);
        ClassMapper classMapper = Mappers.getMapper(ClassMapper.class);
        subjectService = new SubjectServiceImpl(subjectRepository, classRepository, gradeRepository, subjectMapper, classMapper);
    }

    @Test
    void shouldCreateGradeSubjectAndSyncActiveClassesInThatGrade() {
        SubjectDTO request = new SubjectDTO();
        request.setCode("MAT");
        request.setName("Mathematics");
        request.setDescription("Core mathematics curriculum");
        request.setGradeIds(List.of(1L));

        Grade grade = new Grade();
        grade.setId(1L);
        grade.setName("Grade 10");
        grade.setLevel(10);
        grade.setActive(true);

        org.edu.entity.Class schoolClass = new org.edu.entity.Class();
        schoolClass.setId(10L);
        schoolClass.setName("Grade 10A");
        schoolClass.setGrade(grade);
        schoolClass.setActive(true);

        when(subjectRepository.save(any(Subject.class))).thenAnswer(invocation -> {
            Subject subject = invocation.getArgument(0);
            if (subject.getId() == null) {
                subject.setId(4L);
            }
            return subject;
        });
        when(gradeRepository.findAllById(List.of(1L))).thenReturn(List.of(grade));
        when(classRepository.findByGradeIdInAndActiveTrue(List.of(1L))).thenReturn(List.of(schoolClass));
        when(classRepository.findClassesLinkedToSubject(4L)).thenReturn(List.of());

        SubjectDTO saved = subjectService.createSubjects(request);

        assertEquals(List.of(1L), saved.getGradeIds());
        assertEquals(List.of(10L), saved.getClassIds());
        assertTrue(schoolClass.getSubjects().stream().anyMatch(subject -> subject.getId().equals(4L)));
        verify(classRepository).saveAll(List.of(schoolClass));
    }
}
