package org.edu.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.edu.dto.StudentMarkDTO;
import org.edu.entity.Exam;
import org.edu.entity.Staff;
import org.edu.entity.Student;
import org.edu.entity.StudentMark;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.StudentMarkMapper;
import org.edu.repository.ExamRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.StudentMarkRepository;
import org.edu.repository.StudentRepository;
import org.edu.service.StudentMarkService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentMarkServiceImpl implements StudentMarkService {

    private final StudentMarkRepository studentMarkRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final StudentMarkMapper studentMarkMapper;

    @Override
    public StudentMarkDTO createStudentMark(StudentMarkDTO dto) {
        Exam exam = getActiveExam(dto.getExamId());
        Student student = getActiveStudent(dto.getStudentId());
        validateStudentBelongsToExamClass(student, exam);
        validateMarks(dto.getMarksObtained(), exam);
        validateDuplicate(dto.getExamId(), dto.getStudentId(), null);

        StudentMark studentMark = studentMarkMapper.toEntity(dto);
        applyRelations(studentMark, dto, exam, student);
        calculateResult(studentMark, exam);

        return studentMarkMapper.toDTO(studentMarkRepository.save(studentMark));
    }

    @Override
    public StudentMarkDTO updateStudentMark(Long id, StudentMarkDTO dto) {
        StudentMark studentMark = getStudentMark(id);
        Long examId = dto.getExamId() == null ? studentMark.getExam().getId() : dto.getExamId();
        Long studentId = dto.getStudentId() == null ? studentMark.getStudent().getId() : dto.getStudentId();

        Exam exam = getActiveExam(examId);
        Student student = getActiveStudent(studentId);
        validateStudentBelongsToExamClass(student, exam);
        BigDecimal marksObtained = dto.getMarksObtained() == null ? studentMark.getMarksObtained() : dto.getMarksObtained();
        validateMarks(marksObtained, exam);
        validateDuplicate(examId, studentId, id);

        StudentMarkDTO resolved = resolveForUpdate(dto, studentMark, examId, studentId, marksObtained);
        studentMarkMapper.updateEntityFromDTO(resolved, studentMark);
        applyRelations(studentMark, resolved, exam, student);
        calculateResult(studentMark, exam);

        return studentMarkMapper.toDTO(studentMarkRepository.save(studentMark));
    }

    @Override
    public void deleteStudentMark(Long id) {
        studentMarkRepository.delete(getStudentMark(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StudentMarkDTO getStudentMarkById(Long id) {
        return studentMarkMapper.toDTO(getStudentMark(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentMarkDTO> getMarksByExam(Long examId, Pageable pageable) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found with id: " + examId);
        }
        return studentMarkRepository.findByExamIdOrderByStudentNameAsc(examId, pageable)
                .map(studentMarkMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentMarkDTO> getMarksByStudent(Long studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return studentMarkRepository.findByStudentIdOrderByExamExamDateDesc(studentId, pageable)
                .map(studentMarkMapper::toDTO);
    }

    private StudentMark getStudentMark(Long id) {
        return studentMarkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student mark not found with id: " + id));
    }

    private Exam getActiveExam(Long examId) {
        return examRepository.findByIdAndActiveTrue(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Active exam not found with id: " + examId));
    }

    private Student getActiveStudent(Long studentId) {
        return studentRepository.findByIdAndActiveTrue(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Active student not found with id: " + studentId));
    }

    private void applyRelations(StudentMark studentMark, StudentMarkDTO dto, Exam exam, Student student) {
        studentMark.setExam(exam);
        studentMark.setStudent(student);

        if (dto.getEnteredByStaffId() != null) {
            Staff enteredBy = staffRepository.findByIdAndActiveTrue(dto.getEnteredByStaffId())
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + dto.getEnteredByStaffId()));
            studentMark.setEnteredBy(enteredBy);
        }
    }

    private void validateStudentBelongsToExamClass(Student student, Exam exam) {
        if (student.getCurrentClass() == null || !student.getCurrentClass().getId().equals(exam.getStudentClass().getId())) {
            throw new IllegalArgumentException("Student does not belong to the exam class");
        }
    }

    private void validateMarks(BigDecimal marksObtained, Exam exam) {
        if (marksObtained.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Marks obtained cannot be negative");
        }
        if (marksObtained.compareTo(exam.getMaxMarks()) > 0) {
            throw new IllegalArgumentException("Marks obtained cannot exceed exam max marks");
        }
    }

    private void validateDuplicate(Long examId, Long studentId, Long currentId) {
        boolean exists = currentId == null
                ? studentMarkRepository.existsByExamIdAndStudentId(examId, studentId)
                : studentMarkRepository.existsByExamIdAndStudentIdAndIdNot(examId, studentId, currentId);

        if (exists) {
            throw new IllegalStateException("Marks already entered for this student and exam");
        }
    }

    private void calculateResult(StudentMark studentMark, Exam exam) {
        BigDecimal percentage = studentMark.getMarksObtained()
                .multiply(BigDecimal.valueOf(100))
                .divide(exam.getMaxMarks(), 2, RoundingMode.HALF_UP);

        studentMark.setPercentage(percentage);
        studentMark.setGrade(calculateGrade(percentage));
        studentMark.setPassed(studentMark.getMarksObtained().compareTo(exam.getPassMarks()) >= 0);
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

    private StudentMarkDTO resolveForUpdate(
            StudentMarkDTO dto,
            StudentMark studentMark,
            Long examId,
            Long studentId,
            BigDecimal marksObtained
    ) {
        StudentMarkDTO resolved = new StudentMarkDTO();
        resolved.setExamId(examId);
        resolved.setStudentId(studentId);
        resolved.setEnteredByStaffId(dto.getEnteredByStaffId() == null && studentMark.getEnteredBy() != null
                ? studentMark.getEnteredBy().getId()
                : dto.getEnteredByStaffId());
        resolved.setMarksObtained(marksObtained);
        resolved.setRemarks(dto.getRemarks());
        return resolved;
    }
}
