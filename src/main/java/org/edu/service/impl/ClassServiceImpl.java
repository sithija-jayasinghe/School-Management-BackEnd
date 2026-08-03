package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.edu.exception.ResourceNotFoundException;
import org.edu.filter.ClassFilterDefinitions;
import org.edu.filter.FilterSpecifications;
import org.edu.mapper.ClassMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.SubjectRepository;
import org.edu.entity.Grade;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.entity.AcademicYear;
import org.edu.service.ClassService;
import org.edu.util.ClassSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final StaffRepository staffRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public ClassDTO createClass(ClassDTO classDTO) {

        Class clazz = classMapper.toEntity(classDTO);
        applyAcademicYearGradeAndSection(clazz, classDTO, null);
        clazz.setActive(true);

        if (classDTO.getClassTeacherId() != null) {
            Staff teacher = staffRepository.findByIdAndActiveTrue(classDTO.getClassTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + classDTO.getClassTeacherId()));
            clazz.setClassTeacher(teacher);
        }

        if (classDTO.getSubjectIds() != null && !classDTO.getSubjectIds().isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(classDTO.getSubjectIds());
            if (subjects.size() != classDTO.getSubjectIds().size()) {
                throw new ResourceNotFoundException("One or more subjects not found");
            }
            clazz.setSubjects(subjects);
        }

        syncClassTeacherDesignation(clazz);

        return classMapper.toDTO(classRepository.save(clazz));
    }

    @Override
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {

        Class clazz = classRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        classMapper.updateEntityFromDTO(classDTO, clazz);
        applyAcademicYearGradeAndSection(clazz, classDTO, id);

        if (classDTO.getClassTeacherId() != null) {
            Staff teacher = staffRepository.findByIdAndActiveTrue(classDTO.getClassTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + classDTO.getClassTeacherId()));
            clazz.setClassTeacher(teacher);
        }

        if (classDTO.getSubjectIds() != null) {
            if (classDTO.getSubjectIds().isEmpty()) {
                clazz.setSubjects(new java.util.ArrayList<>());
            } else {
                List<Subject> subjects = subjectRepository.findAllById(classDTO.getSubjectIds());
                if (subjects.size() != classDTO.getSubjectIds().size()) {
                    throw new ResourceNotFoundException("One or more subjects not found");
                }
                clazz.setSubjects(subjects);
            }
        }

        syncClassTeacherDesignation(clazz);

        return classMapper.toDTO(clazz);
    }

    @Override
    public void deleteClass(Long id) {

        Class clazz = classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        if (!clazz.isActive()) {
            throw new IllegalStateException("Class already inactive");
        }

        clazz.setActive(false);
        classRepository.save(clazz);
    }

    @Override
    public Page<ClassDTO> getAllClasses(Pageable pageable) {

        return classRepository.findByActiveTrue(pageable)
                .map(classMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClassDTO> filterClasses(Map<String, String> filters, Pageable pageable) {
        if (filters.isEmpty()) {
            return getAllClasses(pageable);
        }

        return classRepository.findAll(FilterSpecifications.build(filters, ClassFilterDefinitions.definitions()), pageable)
                .map(classMapper::toDTO);
    }

    @Override
    public ClassDTO getClassById(Long id) {

        Class clazz = classRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        return classMapper.toDTO(clazz);
    }

    @Override
    public Page<ClassDTO> searchClasses(String name, Pageable pageable) {

        return classRepository
                .searchActiveClasses(name.trim(), pageable)
                .map(classMapper::toDTO);
    }

    @Override
    public List<ClassDTO> getAllActiveClasses() {

        return classRepository.findByActiveTrue()
                .stream()
                .map(classMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassDTO> getCurrentAcademicYearActiveClasses() {
        AcademicYear currentAcademicYear = academicYearRepository.findByCurrentTrueAndActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Current academic year not found"));

        return classRepository.findByAcademicYearIdAndActiveTrueOrderByNameAsc(currentAcademicYear.getId())
                .stream()
                .filter(this::isPrimaryClass)
                .map(classMapper::toDTO)
                .toList();
    }

    private void applyAcademicYearGradeAndSection(Class clazz, ClassDTO classDTO, Long currentClassId) {
        Long academicYearId = classDTO.getAcademicYearId() == null && clazz.getAcademicYear() != null
                ? clazz.getAcademicYear().getId()
                : classDTO.getAcademicYearId();
        Long gradeId = classDTO.getGradeId() == null && clazz.getGrade() != null
                ? clazz.getGrade().getId()
                : classDTO.getGradeId();
        String sectionValue = classDTO.getSection() == null && clazz.getSection() != null
                ? clazz.getSection()
                : classDTO.getSection();

        if (academicYearId == null || gradeId == null || sectionValue == null || sectionValue.isBlank()) {
            throw new IllegalArgumentException("Academic year, grade, and section are required for classes");
        }

        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));

        Grade grade = gradeRepository.findByIdAndActiveTrue(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));

        if (grade.getLevel() == null || grade.getLevel() < 1 || grade.getLevel() > 5) {
            throw new IllegalArgumentException("Only Grade 1 to Grade 5 classes are allowed");
        }

        ClassSection section = parseSection(sectionValue);
        String sectionName = section.name();
        boolean duplicate = currentClassId == null
                ? classRepository.existsByAcademicYearIdAndGradeIdAndSectionAndActiveTrue(academicYearId, gradeId, sectionName)
                : classRepository.existsByAcademicYearIdAndGradeIdAndSectionAndIdNotAndActiveTrue(
                        academicYearId,
                        gradeId,
                        sectionName,
                        currentClassId
                );
        if (duplicate) {
            throw new IllegalStateException("Class already exists for this academic year, grade, and section");
        }

        clazz.setAcademicYear(academicYear);
        clazz.setGrade(grade);
        clazz.setSection(sectionName);
        clazz.setName(grade.getName() + " " + section);
    }

    private ClassSection parseSection(String section) {
        try {
            return ClassSection.valueOf(section.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Class section must be A, B, C, D, or E");
        }
    }

    private boolean isPrimaryClass(Class clazz) {
        return clazz.getGrade() != null
                && clazz.getGrade().getLevel() != null
                && clazz.getGrade().getLevel() >= 1
                && clazz.getGrade().getLevel() <= 5
                && clazz.getSection() != null
                && java.util.Arrays.stream(ClassSection.values())
                        .anyMatch(section -> section.name().equalsIgnoreCase(clazz.getSection()));
    }

    private void syncClassTeacherDesignation(Class clazz) {
        if (clazz.getClassTeacher() == null || clazz.getName() == null || clazz.getName().isBlank()) {
            return;
        }
        clazz.getClassTeacher().setDesignation(clazz.getName().trim() + " Class Teacher");
    }
}
