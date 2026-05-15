package org.edu.mapper;

import org.edu.dto.AcademicYearDTO;
import org.edu.entity.AcademicYear;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicYearMapper extends BaseMapper<AcademicYearDTO, AcademicYear> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "terms", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AcademicYear toEntity(AcademicYearDTO dto);

    @Override
    AcademicYearDTO toDTO(AcademicYear academicYear);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "terms", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(AcademicYearDTO dto, @MappingTarget AcademicYear academicYear);
}
