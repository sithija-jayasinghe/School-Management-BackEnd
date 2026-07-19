package org.edu.mapper;

import org.edu.dto.StaffDTO;
import org.edu.entity.Staff;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffMapper extends BaseMapper<StaffDTO, Staff> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Staff toEntity(StaffDTO dto);

    @Override
    @Mapping(target = "userId", expression = "java(staff.getUser() == null ? null : staff.getUser().getId())")
    @Mapping(target = "staffCategory", expression = "java(staff.getStaffCategory() == null ? legacyCategory(staff) : staff.getStaffCategory())")
    @Mapping(target = "employmentType", expression = "java(staff.getEmploymentType() == null ? org.edu.util.EmploymentType.PERMANENT : staff.getEmploymentType())")
    @Mapping(target = "teachingCapable", expression = "java(isTeachingCapable(staff))")
    StaffDTO toDTO(Staff staff);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "staffId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(StaffDTO dto, @MappingTarget Staff staff);

    default org.edu.util.StaffCategory legacyCategory(Staff staff) {
        if (staff.getUser() != null && staff.getUser().getRole() == org.edu.util.Role.TEACHER) {
            return org.edu.util.StaffCategory.ACADEMIC;
        }
        return org.edu.util.StaffCategory.SUPPORT;
    }

    default boolean isTeachingCapable(Staff staff) {
        if (staff.getTeachingCapable() != null) {
            return staff.getTeachingCapable();
        }
        return staff.getUser() != null && staff.getUser().getRole() == org.edu.util.Role.TEACHER;
    }
}
