package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.ClassMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.SubjectRepository;
import org.edu.entity.Grade;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.service.ClassService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;
    private final ClassMapper classMapper;
    private final GradeRepository gradeRepository;
    private final StaffRepository staffRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public ClassDTO createClass(ClassDTO classDTO) {

        Class clazz = classMapper.toEntity(classDTO);
        applyGradeAndSection(clazz, classDTO);
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

        return classMapper.toDTO(classRepository.save(clazz));
    }

    @Override
    public ClassDTO updateClass(Long id, ClassDTO classDTO) {

        Class clazz = classRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + id));

        classMapper.updateEntityFromDTO(classDTO, clazz);
        applyGradeAndSection(clazz, classDTO);

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
    }

    @Override
    public Page<ClassDTO> getAllClasses(Pageable pageable) {

        return classRepository.findByActiveTrue(pageable)
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

    private void applyGradeAndSection(Class clazz, ClassDTO classDTO) {
        if (classDTO.getGradeId() == null) {
            return;
        }

        Grade grade = gradeRepository.findByIdAndActiveTrue(classDTO.getGradeId())
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + classDTO.getGradeId()));
        String section = classDTO.getSection() == null ? "" : classDTO.getSection().trim().toUpperCase();

        if (section.isBlank()) {
            throw new IllegalArgumentException("Class section is required when a grade is selected");
        }

        clazz.setGrade(grade);
        clazz.setSection(section);
        clazz.setName(grade.getName() + " " + section);
    }
}
