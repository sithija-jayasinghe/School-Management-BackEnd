package org.edu.mapper;

import org.edu.dto.SubjectDTO;
import org.edu.entity.Subject;
import org.edu.entity.Class;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubjectMapper extends BaseMapper<SubjectDTO, Subject> {

    @Mapping(target = "id", ignore = true)
    Subject toEntity(SubjectDTO dto);

    @Mapping(target = "classIds", source = "classes")
    SubjectDTO toDTO(Subject subject);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(SubjectDTO dto, @MappingTarget Subject subject);

    default List<Long> mapClassesToIds(List<Class> classes) {
        if (classes == null) {
            return null;
        }

        return classes.stream()
                .map(Class::getId)
                .toList();
    }
}
