package org.edu.mapper;

import org.edu.dto.SubjectDTO;
import org.edu.entity.Subject;
import org.edu.entity.Class;
import org.edu.entity.Grade;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper extends BaseMapper<SubjectDTO, Subject> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grades", ignore = true)
    @Mapping(target = "classes", ignore = true)
    Subject toEntity(SubjectDTO dto);

    @Mapping(target = "gradeIds", source = "grades")
    @Mapping(target = "classIds", source = "classes")
    SubjectDTO toDTO(Subject subject);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "grades", ignore = true)
    @Mapping(target = "classes", ignore = true)
    void updateEntityFromDTO(SubjectDTO dto, @MappingTarget Subject subject);

    default List<Long> mapGradesToIds(List<Grade> grades) {
        if (grades == null) {
            return null;
        }

        return grades.stream()
                .map(Grade::getId)
                .toList();
    }

    default List<Long> mapClassesToIds(List<Class> classes) {
        if (classes == null) {
            return null;
        }

        return classes.stream()
                .map(Class::getId)
                .toList();
    }
}
