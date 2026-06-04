package org.edu.service.impl;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.edu.service.ExamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AcademicTermRepository academicTermRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final ExamMapper examMapper;

    @Override
    public ExamDTO createExam(ExamDTO dto) {
        validateMarks(dto.getMaxMarks(), dto.getPassMarks());
        validateDuplicate(dto, null);

        Exam exam = examMapper.toEntity(dto);
        applyRelations(exam, dto);
        exam.setActive(true);

        return examMapper.toDTO(examRepository.save(exam));
    }

    @Override
    public ExamDTO updateExam(Long id, ExamDTO dto) {
        Exam exam = examRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        ExamDTO resolved = resolveForUpdate(dto, exam);

        validateMarks(resolved.getMaxMarks(), resolved.getPassMarks());
        validateDuplicate(resolved, id);

        examMapper.updateEntityFromDTO(dto, exam);
        applyRelations(exam, resolved);

        return examMapper.toDTO(examRepository.save(exam));
    }

    @Override
    public void deactivateExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        if (!exam.isActive()) {
            throw new IllegalStateException("Exam already inactive");
        }
        exam.setActive(false);
    }

    @Override
    public void activateExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
        if (exam.isActive()) {
            throw new IllegalStateException("Exam already active");
        }
        exam.setActive(true);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamDTO getExamById(Long id) {
        return examRepository.findByIdAndActiveTrue(id)
                .map(examMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamDTO> getAllExams(Pageable pageable) {
        return examRepository.findByActiveTrue(pageable)
                .map(examMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamDTO> searchExams(String name, Pageable pageable) {
        return examRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                .map(examMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamDTO> getExamsByClass(Long classId, Pageable pageable) {
        return examRepository.findByStudentClassIdAndActiveTrue(classId, pageable)
                .map(examMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamDTO> getExamsByAcademicTerm(Long academicTermId) {
        return examRepository.findByAcademicTermIdAndActiveTrueOrderByExamDateAsc(academicTermId)
                .stream()
                .map(examMapper::toDTO)
                .toList();
    }

    private void applyRelations(Exam exam, ExamDTO dto) {
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(dto.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + dto.getAcademicYearId()));
        AcademicTerm academicTerm = academicTermRepository.findByIdAndActiveTrue(dto.getAcademicTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with id: " + dto.getAcademicTermId()));
        org.edu.entity.Class studentClass = classRepository.findByIdAndActiveTrue(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + dto.getClassId()));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));

        validateTermBelongsToYear(academicTerm, academicYear);
        validateExamDateInsideTerm(dto, academicTerm);

        exam.setAcademicYear(academicYear);
        exam.setAcademicTerm(academicTerm);
        exam.setStudentClass(studentClass);
        exam.setSubject(subject);
    }

    private void validateTermBelongsToYear(AcademicTerm academicTerm, AcademicYear academicYear) {
        if (!academicTerm.getAcademicYear().getId().equals(academicYear.getId())) {
            throw new IllegalArgumentException("Academic term does not belong to selected academic year");
        }
    }

    private void validateExamDateInsideTerm(ExamDTO dto, AcademicTerm academicTerm) {
        if (dto.getExamDate().isBefore(academicTerm.getStartDate()) || dto.getExamDate().isAfter(academicTerm.getEndDate())) {
            throw new IllegalArgumentException("Exam date must be within the academic term date range");
        }
    }

    private void validateMarks(BigDecimal maxMarks, BigDecimal passMarks) {
        if (maxMarks.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Max marks must be greater than zero");
        }
        if (passMarks.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Pass marks cannot be negative");
        }
        if (passMarks.compareTo(maxMarks) > 0) {
            throw new IllegalArgumentException("Pass marks cannot be greater than max marks");
        }
    }

    private void validateDuplicate(ExamDTO dto, Long currentId) {
        boolean exists = currentId == null
                ? examRepository.existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCase(
                        dto.getAcademicTermId(),
                        dto.getClassId(),
                        dto.getSubjectId(),
                        dto.getName()
                )
                : examRepository.existsByAcademicTermIdAndStudentClassIdAndSubjectIdAndNameIgnoreCaseAndIdNot(
                        dto.getAcademicTermId(),
                        dto.getClassId(),
                        dto.getSubjectId(),
                        dto.getName(),
                        currentId
                );

        if (exists) {
            throw new IllegalStateException("Exam already exists for this term, class, subject, and name");
        }
    }

    private ExamDTO resolveForUpdate(ExamDTO dto, Exam exam) {
        ExamDTO resolved = new ExamDTO();
        resolved.setAcademicYearId(dto.getAcademicYearId() == null ? exam.getAcademicYear().getId() : dto.getAcademicYearId());
        resolved.setAcademicTermId(dto.getAcademicTermId() == null ? exam.getAcademicTerm().getId() : dto.getAcademicTermId());
        resolved.setClassId(dto.getClassId() == null ? exam.getStudentClass().getId() : dto.getClassId());
        resolved.setSubjectId(dto.getSubjectId() == null ? exam.getSubject().getId() : dto.getSubjectId());
        resolved.setName(dto.getName() == null ? exam.getName() : dto.getName());
        resolved.setExamDate(dto.getExamDate() == null ? exam.getExamDate() : dto.getExamDate());
        resolved.setMaxMarks(dto.getMaxMarks() == null ? exam.getMaxMarks() : dto.getMaxMarks());
        resolved.setPassMarks(dto.getPassMarks() == null ? exam.getPassMarks() : dto.getPassMarks());
        return resolved;
    }
}
