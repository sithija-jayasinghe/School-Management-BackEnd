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
import org.edu.repository.SubjectRepository;
import org.edu.service.SubjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final ClassRepository classRepository;
    private final SubjectMapper subjectMapper;
    private final ClassMapper classMapper;

    @Override
    public SubjectDTO createSubjects(SubjectDTO dto) {
        Subject subject = subjectMapper.toEntity(dto);
        Subject savedSubject = subjectRepository.save(subject);
        
        if (dto.getClassIds() != null && !dto.getClassIds().isEmpty()) {
            List<Class> classes = classRepository.findAllById(dto.getClassIds());
            if (classes.size() != dto.getClassIds().size()) {
                throw new ResourceNotFoundException("One or more classes not found");
            }
            savedSubject.setClasses(classes);
            savedSubject = subjectRepository.save(savedSubject);
        }
        
        return subjectMapper.toDTO(savedSubject);
    }

    @Override
    public Page<SubjectDTO> getAllSubjects(Pageable pageable) {
        return subjectRepository.findAll(pageable)
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

        if (dto.getClassIds() != null) {
            if (dto.getClassIds().isEmpty()) {
                subject.setClasses(new java.util.ArrayList<>());
            } else {
                List<Class> classes = classRepository.findAllById(dto.getClassIds());
                if (classes.size() != dto.getClassIds().size()) {
                    throw new ResourceNotFoundException("One or more classes not found");
                }
                subject.setClasses(classes);
            }
        }

        Subject updated = subjectRepository.save(subject);

        return subjectMapper.toDTO(updated);
    }

    @Override
    public void deleteSubjects(Long id) {
        subjectRepository.deleteById(id);
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

        clazz.getSubjects().add(subject);
        classRepository.save(clazz);
    }

    @Override
    public void removeSubjectFromClass(Long classId, Long subjectId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        clazz.getSubjects().removeIf(s -> s.getId().equals(subjectId));

        classRepository.save(clazz);
    }

    @Override
    public List<SubjectDTO> getSubjectsByClass(Long classId) {

        Class clazz = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        return clazz.getSubjects()
                .stream()
                .map(subjectMapper::toDTO)
                .toList();
    }

    @Override
    public List<ClassDTO> getClassesBySubject(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        return subject.getClasses()
                .stream()
                .map(classMapper::toDTO)
                .toList();
    }
}
