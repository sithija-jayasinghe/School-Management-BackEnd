package org.edu.mapper;

import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassMapper extends BaseMapper<ClassDTO, Class> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classTeacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Class toEntity(ClassDTO dto);

    @Override
    @Mapping(target = "classTeacherId", source = "classTeacher.id")
    @Mapping(target = "classTeacherName", source = "classTeacher.name")
    ClassDTO toDTO(Class clazz);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "classTeacher", ignore = true)
    @Mapping(target = "students", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ClassDTO dto, @MappingTarget Class clazz);
}
