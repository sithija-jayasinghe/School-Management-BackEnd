package org.edu.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.edu.entity.Grade;
import org.edu.repository.ClassRepository;
import org.edu.repository.GradeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class GradeSeeder implements CommandLineRunner {

    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^Grade\\s+(\\d+)\\s+(.+)$", Pattern.CASE_INSENSITIVE);

    private final GradeRepository gradeRepository;
    private final ClassRepository classRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (gradeRepository.count() == 0) {
            for (int level = 1; level <= 5; level++) {
                Grade grade = new Grade();
                grade.setName("Grade " + level);
                grade.setLevel(level);
                grade.setActive(true);
                gradeRepository.save(grade);
            }
        }

        backfillExistingClassSections();
    }

    private void backfillExistingClassSections() {
        classRepository.findAll().stream()
                .filter(studentClass -> studentClass.getGrade() == null && studentClass.getName() != null)
                .forEach(studentClass -> {
                    Matcher matcher = CLASS_NAME_PATTERN.matcher(studentClass.getName().trim());
                    if (!matcher.matches()) {
                        return;
                    }

                    int level = Integer.parseInt(matcher.group(1));
                    String section = matcher.group(2).trim();
                    gradeRepository.findByLevel(level).ifPresent(grade -> {
                        studentClass.setGrade(grade);
                        studentClass.setSection(section);
                        studentClass.setName(grade.getName() + " " + section);
                        classRepository.save(studentClass);
                    });
                });
    }
}
