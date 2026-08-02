package org.edu.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassTeacherAssignmentRequest;
import org.edu.dto.TeachingAssignmentDTO;
import org.edu.entity.AcademicYear;
import org.edu.entity.Staff;
import org.edu.entity.Subject;
import org.edu.entity.TeachingAssignment;
import org.edu.exception.ResourceNotFoundException;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.ClassRepository;
import org.edu.repository.StaffRepository;
import org.edu.repository.SubjectRepository;
import org.edu.repository.TeachingAssignmentRepository;
import org.edu.service.TeachingAssignmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {

    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final StaffRepository staffRepository;
    private final ClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public TeachingAssignmentDTO createAssignment(TeachingAssignmentDTO dto) {
        TeachingAssignment assignment = new TeachingAssignment();
        applyAssignmentFields(assignment, dto, null);
        return toDTO(teachingAssignmentRepository.save(assignment));
    }

    @Override
    public List<TeachingAssignmentDTO> assignClassTeacher(ClassTeacherAssignmentRequest request) {
        Staff staff = staffRepository.findByIdAndActiveTrue(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Active staff not found with id: " + request.getStaffId()));
        org.edu.entity.Class studentClass = classRepository.findByIdAndActiveTrue(request.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Active class not found with id: " + request.getClassId()));
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(request.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Active academic year not found with id: " + request.getAcademicYearId()));

        if (studentClass.getClassTeacher() != null
                && !studentClass.getClassTeacher().getId().equals(staff.getId())) {
            throw new IllegalStateException("This class already has a class teacher assigned");
        }

        if (studentClass.getClassTeacher() == null) {
            studentClass.setClassTeacher(staff);
            classRepository.save(studentClass);
        }

        List<TeachingAssignmentDTO> created = new ArrayList<>();
        for (Long subjectId : request.getSubjectIds()) {
            Subject subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

            boolean subjectAlreadyAssigned = teachingAssignmentRepository
                    .existsByStudentClassIdAndSubjectIdAndAcademicYearIdAndActiveTrue(
                            studentClass.getId(), subject.getId(), academicYear.getId());
            boolean alreadyAssigned = teachingAssignmentRepository
                    .existsByStaffIdAndStudentClassIdAndSubjectIdAndAcademicYearId(
                            staff.getId(), studentClass.getId(), subject.getId(), academicYear.getId());
            if (alreadyAssigned) {
                throw new IllegalStateException("Teacher is already assigned to this class subject for the academic year");
            }
            if (subjectAlreadyAssigned) {
                throw new IllegalStateException("This subject already has a teacher assigned for this class and academic year");
            }

            TeachingAssignment assignment = new TeachingAssignment();
            assignment.setStaff(staff);
            assignment.setStudentClass(studentClass);
            assignment.setSubject(subject);
            assignment.setAcademicYear(academicYear);
            assignment.setActive(true);
            created.add(toDTO(teachingAssignmentRepository.save(assignment)));
        }
        return created;
    }

    @Override
    public TeachingAssignmentDTO updateAssignment(Long id, TeachingAssignmentDTO dto) {
        TeachingAssignment assignment = teachingAssignmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));
        applyAssignmentFields(assignment, dto, id);
        return toDTO(teachingAssignmentRepository.save(assignment));
    }

    @Override
    public void deactivateAssignment(Long id) {
        TeachingAssignment assignment = teachingAssignmentRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));
        assignment.setActive(false);
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingAssignmentDTO getAssignmentById(Long id) {
        return teachingAssignmentRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Teaching assignment not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachingAssignmentDTO> getAssignments(Pageable pageable) {
        return teachingAssignmentRepository.findByActiveTrue(pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeachingAssignmentDTO> searchAssignments(String keyword, Pageable pageable) {
        return teachingAssignmentRepository.searchActiveAssignments(keyword, pageable).map(this::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentDTO> getAssignmentsByTeacher(Long staffId) {
        return teachingAssignmentRepository.findByStaffIdAndActiveTrue(staffId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentDTO> getAssignmentsByClass(Long classId) {
        return teachingAssignmentRepository.findByStudentClassIdAndActiveTrue(classId).stream().map(this::toDTO).toList();
    }

    private void applyAssignmentFields(TeachingAssignment assignment, TeachingAssignmentDTO dto, Long currentId) {
        Staff staff = staffRepository.findByIdAndActiveTrue(dto.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Active staff not found with id: " + dto.getStaffId()));
        org.edu.entity.Class studentClass = classRepository.findByIdAndActiveTrue(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Active class not found with id: " + dto.getClassId()));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));
        AcademicYear academicYear = academicYearRepository.findByIdAndActiveTrue(dto.getAcademicYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Active academic year not found with id: " + dto.getAcademicYearId()));

        boolean sameTeacherDuplicate = currentId == null
                ? teachingAssignmentRepository.existsByStaffIdAndStudentClassIdAndSubjectIdAndAcademicYearId(
                        staff.getId(), studentClass.getId(), subject.getId(), academicYear.getId())
                : teachingAssignmentRepository.existsByStaffIdAndStudentClassIdAndSubjectIdAndAcademicYearIdAndIdNot(
                        staff.getId(), studentClass.getId(), subject.getId(), academicYear.getId(), currentId);
        if (sameTeacherDuplicate) {
            throw new IllegalStateException("Teacher is already assigned to this class subject for the academic year");
        }

        boolean subjectDuplicate = currentId == null
                ? teachingAssignmentRepository.existsByStudentClassIdAndSubjectIdAndAcademicYearIdAndActiveTrue(
                        studentClass.getId(), subject.getId(), academicYear.getId())
                : teachingAssignmentRepository.existsByStudentClassIdAndSubjectIdAndAcademicYearIdAndActiveTrueAndIdNot(
                        studentClass.getId(), subject.getId(), academicYear.getId(), currentId);
        if (dto.isActive() && subjectDuplicate) {
            throw new IllegalStateException("This subject already has a teacher assigned for this class and academic year");
        }

        assignment.setStaff(staff);
        assignment.setStudentClass(studentClass);
        assignment.setSubject(subject);
        assignment.setAcademicYear(academicYear);
        assignment.setActive(dto.isActive());
    }

    private TeachingAssignmentDTO toDTO(TeachingAssignment assignment) {
        return new TeachingAssignmentDTO(
                assignment.getId(),
                assignment.getStaff().getId(),
                assignment.getStaff().getName(),
                assignment.getStudentClass().getId(),
                assignment.getStudentClass().getName(),
                // DEMO-FEATURE: teaching-assignment-grade-filter START
                // Purpose: Sends grade details from the assignment class to support frontend grade filtering.
                // assignment.getStudentClass().getGrade() == null ? null : assignment.getStudentClass().getGrade().getId(),
                // assignment.getStudentClass().getGrade() == null ? null : assignment.getStudentClass().getGrade().getName(),
                // DEMO-FEATURE: teaching-assignment-grade-filter END
                assignment.getSubject().getId(),
                assignment.getSubject().getCode(),
                assignment.getSubject().getName(),
                assignment.getAcademicYear().getId(),
                assignment.getAcademicYear().getName(),
                assignment.isActive(),
                assignment.getCreatedAt(),
                assignment.getUpdatedAt()
        );
    }
}
