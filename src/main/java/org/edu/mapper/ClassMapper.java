package org.edu.mapper;

import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.edu.entity.Subject; // Assuming your Subject entity is here
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassMapper extends BaseMapper<ClassDTO, Class> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "classTeacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Class toEntity(ClassDTO dto);

    @Override
    @Mapping(target = "classTeacherId", source = "classTeacher.id")
    @Mapping(target = "classTeacherName", source = "classTeacher.name")
    @Mapping(target = "gradeId", source = "grade.id")
    @Mapping(target = "gradeName", source = "grade.name")
    @Mapping(target = "gradeLevel", source = "grade.level")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearName", source = "academicYear.name")
    @Mapping(target = "subjectIds", source = "subjects")
    ClassDTO toDTO(Class clazz);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "classTeacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "subjects", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ClassDTO dto, @MappingTarget Class clazz);


    default List<Long> mapSubjectsToIds(List<Subject> subjects) {
        if (subjects == null) {
            return null;
        }
        return subjects.stream()
                .map(Subject::getId)
                .collect(Collectors.toList());
    }
}
