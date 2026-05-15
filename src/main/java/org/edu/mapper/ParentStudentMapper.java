package org.edu.mapper;

import org.edu.dto.ParentStudentDTO;
import org.edu.entity.ParentStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParentStudentMapper {
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "student.id", target = "studentId")
    ParentStudentDTO toDTO(ParentStudent entity);

    @Mapping(source = "parentId", target = "parent.id")
    @Mapping(source = "studentId", target = "student.id")
    ParentStudent toEntity(ParentStudentDTO dto);
}
