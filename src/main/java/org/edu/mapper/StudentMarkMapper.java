package org.edu.mapper;

import org.edu.dto.StudentMarkDTO;
import org.edu.entity.StudentMark;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMarkMapper extends BaseMapper<StudentMarkDTO, StudentMark> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "enteredBy", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "passed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    StudentMark toEntity(StudentMarkDTO dto);

    @Override
    @Mapping(source = "exam.id", target = "examId")
    @Mapping(source = "exam.name", target = "examName")
    @Mapping(source = "exam.maxMarks", target = "maxMarks")
    @Mapping(source = "exam.studentClass.id", target = "classId")
    @Mapping(source = "exam.studentClass.name", target = "className")
    @Mapping(source = "exam.subject.id", target = "subjectId")
    @Mapping(source = "exam.subject.name", target = "subjectName")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "enteredBy.id", target = "enteredByStaffId")
    @Mapping(source = "enteredBy.name", target = "enteredByStaffName")
    StudentMarkDTO toDTO(StudentMark studentMark);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exam", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "enteredBy", ignore = true)
    @Mapping(target = "percentage", ignore = true)
    @Mapping(target = "grade", ignore = true)
    @Mapping(target = "passed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(StudentMarkDTO dto, @MappingTarget StudentMark studentMark);
}
