package org.edu.mapper;

import org.edu.dto.ExamDTO;
import org.edu.entity.Exam;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamMapper extends BaseMapper<ExamDTO, Exam> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "academicTerm", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Exam toEntity(ExamDTO dto);

    @Override
    @Mapping(source = "academicYear.id", target = "academicYearId")
    @Mapping(source = "academicYear.name", target = "academicYearName")
    @Mapping(source = "academicTerm.id", target = "academicTermId")
    @Mapping(source = "academicTerm.name", target = "academicTermName")
    @Mapping(source = "studentClass.id", target = "classId")
    @Mapping(source = "studentClass.name", target = "className")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    ExamDTO toDTO(Exam exam);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "academicYear", ignore = true)
    @Mapping(target = "academicTerm", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ExamDTO dto, @MappingTarget Exam exam);
}
