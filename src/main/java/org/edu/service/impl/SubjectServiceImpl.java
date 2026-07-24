package org.edu.service.impl;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.dto.SubjectDTO;
import org.edu.entity.*;
import org.edu.entity.Class;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.ClassMapper;
import org.edu.mapper.SubjectMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.edu.repository.SubjectRepository;
import org.edu.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final GradeRepository gradeRepository;
    private final SubjectMapper subjectMapper;
    private final ClassMapper classMapper;

    @Override
    public SubjectDTO createSubjects(SubjectDTO dto) {
        Subject subject = subjectMapper.toEntity(dto);
        Subject savedSubject = subjectRepository.save(subject);

        applySubjectLinks(savedSubject, dto);

        return subjectMapper.toDTO(savedSubject);
    }

    @Override
    public Page<SubjectDTO> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(subjectMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubjectDTO> filterSubjects(String keyword, Long gradeId, Boolean hasClassCoverage, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.trim().isEmpty() ? null : keyword.trim();
        return subjectRepository.filterSubjects(normalizedKeyword, gradeId, hasClassCoverage, pageable)
                .map(subjectMapper::toDTO);
    }

    @Override
    public SubjectDTO getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return subjectMapper.toDTO(subject);
    }

    @Override
    public SubjectDTO updateSubjects(Long id, SubjectDTO dto) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        subjectMapper.updateEntityFromDTO(dto, subject);
        Subject updated = subjectRepository.save(subject);

        applySubjectLinks(updated, dto);

        return subjectMapper.toDTO(updated);
    }

    private void applySubjectLinks(Subject subject, SubjectDTO dto) {
        if (dto.getGradeIds() != null) {
            List<Grade> grades = resolveGrades(dto.getGradeIds());
            subject.setGrades(grades);
            subjectRepository.save(subject);

            List<Class> classes = grades.isEmpty()
                    ? List.of()
                    : classRepository.findByGradeIdInAndActiveTrue(grades.stream().map(Grade::getId).toList());
            syncClassLinks(subject, classes);
            return;
        }

        if (dto.getClassIds() != null) {
            syncClassLinks(subject, resolveClasses(dto.getClassIds()));
        }
    }

    private List<Grade> resolveGrades(List<Long> gradeIds) {
        List<Long> uniqueIds = distinctIds(gradeIds);
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        List<Grade> grades = gradeRepository.findAllById(uniqueIds).stream()
                .filter(Grade::isActive)
                .toList();
        if (grades.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more grades not found");
        }

        return grades;
    }

    private List<Class> resolveClasses(List<Long> classIds) {
        List<Long> uniqueIds = distinctIds(classIds);
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        List<Class> classes = classRepository.findAllById(uniqueIds);
        if (classes.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more classes not found");
        }

        return classes;
    }

    private List<Long> distinctIds(List<Long> ids) {
        return ids == null
                ? List.of()
                : ids.stream().distinct().toList();
    }

    private void syncClassLinks(Subject subject, List<Class> targetClasses) {
        List<Class> existingClasses = subject.getId() == null
                ? List.of()
                : classRepository.findClassesLinkedToSubject(subject.getId());
        Set<Long> targetIds = targetClasses.stream().map(Class::getId).collect(java.util.stream.Collectors.toSet());
        List<Class> changedClasses = new ArrayList<>();

        for (Class existingClass : existingClasses) {
            if (!targetIds.contains(existingClass.getId()) && ensureSubjects(existingClass).removeIf(existingSubject -> existingSubject.getId().equals(subject.getId()))) {
                changedClasses.add(existingClass);
            }
        }

        Set<Long> existingIds = new HashSet<>(existingClasses.stream().map(Class::getId).toList());
        for (Class targetClass : targetClasses) {
            List<Subject> subjects = ensureSubjects(targetClass);
            boolean alreadyLinked = existingIds.contains(targetClass.getId())
                    || subjects.stream().anyMatch(existingSubject -> existingSubject.getId().equals(subject.getId()));
            if (!alreadyLinked) {
                subjects.add(subject);
                changedClasses.add(targetClass);
            }
        }

        if (!changedClasses.isEmpty()) {
            classRepository.saveAll(changedClasses);
        }

        subject.setClasses(targetClasses);
    }

    private List<Subject> ensureSubjects(Class clazz) {
        if (clazz.getSubjects() == null) {
            clazz.setSubjects(new ArrayList<>());
        }

        return clazz.getSubjects();
    }

    @Override
    public void deleteSubjects(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        List<Class> linkedClasses = classRepository.findClassesLinkedToSubject(id);
        for (Class linkedClass : linkedClasses) {
            ensureSubjects(linkedClass).removeIf(existingSubject -> existingSubject.getId().equals(id));
        }
        if (!linkedClasses.isEmpty()) {
            classRepository.saveAll(linkedClasses);
        }

        subject.setClasses(List.of());
        subject.setGrades(List.of());
        subjectRepository.save(subject);
        subjectRepository.delete(subject);
    }

    @Override
    public Page<SubjectDTO> searchSubjects(String keyword, Pageable pageable) {
        return subjectRepository
                .findByNameContainingIgnoreCase(keyword, pageable)
                .map(subjectMapper::toDTO);
    }

    @Override
    public SubjectDTO getSubjectsByCodeId(String code) {
        Subject subject = subjectRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        return subjectMapper.toDTO(subject);
    }
    @Override
    public void assignSubjectToClass(Long classId, Long subjectId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Subject> subjects = ensureSubjects(clazz);
        if (subjects.stream().noneMatch(existingSubject -> existingSubject.getId().equals(subjectId))) {
            subjects.add(subject);
        }
        classRepository.save(clazz);
    }

    @Override
    public void removeSubjectFromClass(Long classId, Long subjectId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        ensureSubjects(clazz).removeIf(s -> s.getId().equals(subjectId));

        classRepository.save(clazz);
    }

    @Override
    public List<SubjectDTO> getSubjectsByClass(Long classId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return ensureSubjects(clazz)
                .stream()
                .map(subjectMapper::toDTO)
                .toList();
    }

    @Override
    public List<ClassDTO> getClassesBySubject(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        List<Class> classes = subject.getClasses() == null
                ? classRepository.findClassesLinkedToSubject(subjectId)
                : subject.getClasses();

        return classes
                .stream()
                .map(classMapper::toDTO)
                .toList();
    }
}
