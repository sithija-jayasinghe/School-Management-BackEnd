package org.edu.mapper;

import org.edu.dto.AcademicTermDTO;
import org.edu.entity.AcademicTerm;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AcademicTermMapper extends BaseMapper<AcademicTermDTO, AcademicTerm> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AcademicTerm toEntity(AcademicTermDTO dto);

    @Override
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearName", source = "academicYear.name")
    AcademicTermDTO toDTO(AcademicTerm academicTerm);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "current", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(AcademicTermDTO dto, @MappingTarget AcademicTerm academicTerm);
}
