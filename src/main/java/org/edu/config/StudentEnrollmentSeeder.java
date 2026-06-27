package org.edu.config;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.edu.entity.AcademicYear;
import org.edu.entity.Student;
import org.edu.entity.StudentEnrollment;
import org.edu.repository.AcademicYearRepository;
import org.edu.repository.StudentEnrollmentRepository;
import org.edu.repository.StudentRepository;
import org.edu.util.EnrollmentStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class StudentEnrollmentSeeder implements CommandLineRunner {

    private final AcademicYearRepository academicYearRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        academicYearRepository.findByCurrentTrueAndActiveTrue().ifPresent(this::backfillCurrentYearEnrollments);
    }

    private void backfillCurrentYearEnrollments(AcademicYear currentAcademicYear) {
        studentRepository.findByActiveTrue().stream()
                .filter(student -> student.getCurrentClass() != null)
                .forEach(student -> ensureEnrollment(student, currentAcademicYear));
    }

    private void ensureEnrollment(Student student, AcademicYear currentAcademicYear) {
        studentEnrollmentRepository
                .findByStudentIdAndAcademicYearIdAndStatus(
                        student.getId(),
                        currentAcademicYear.getId(),
                        EnrollmentStatus.ACTIVE
                )
                .orElseGet(() -> {
                    StudentEnrollment enrollment = new StudentEnrollment();
                    enrollment.setStudent(student);
                    enrollment.setAcademicYear(currentAcademicYear);
                    enrollment.setStudentClass(student.getCurrentClass());
                    enrollment.setStartDate(currentAcademicYear.getStartDate() != null
                            ? currentAcademicYear.getStartDate()
                            : LocalDate.now());
                    enrollment.setStatus(EnrollmentStatus.ACTIVE);
                    return studentEnrollmentRepository.save(enrollment);
                });
    }
}
