package org.edu.mapper;

import org.edu.dto.AttendanceDTO;
import org.edu.entity.Attendance;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper extends BaseMapper<AttendanceDTO, Attendance> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "timetable", ignore = true)
    @Mapping(target = "markedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Attendance toEntity(AttendanceDTO dto);

    @Override
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "studentClass.id", target = "classId")
    @Mapping(source = "studentClass.name", target = "className")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(source = "subject.name", target = "subjectName")
    @Mapping(source = "timetable.id", target = "timetableId")
    @Mapping(source = "markedBy.id", target = "markedByStaffId")
    @Mapping(source = "markedBy.name", target = "markedByStaffName")
    AttendanceDTO toDTO(Attendance attendance);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "studentClass", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "timetable", ignore = true)
    @Mapping(target = "markedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(AttendanceDTO dto, @MappingTarget Attendance attendance);
}
