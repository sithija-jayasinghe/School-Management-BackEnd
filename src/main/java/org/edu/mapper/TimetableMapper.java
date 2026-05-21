package org.edu.mapper;

import org.edu.dto.TimetableDTO;
import org.edu.entity.Timetable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimetableMapper {

    @Mapping(source = "studentClass.id", target = "classId")
    @Mapping(source = "studentClass.name", target = "className")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.name", target = "staffName")
    TimetableDTO toDTO(Timetable timetable);

    @Mapping(source = "classId", target = "studentClass.id")
    @Mapping(source = "subjectId", target = "subject.id")
    @Mapping(source = "staffId", target = "staff.id")
    Timetable toEntity(TimetableDTO dto);
}

