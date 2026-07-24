package org.edu.mapper;

import org.edu.dto.NoticeDTO;
import org.edu.entity.Notice;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoticeMapper extends BaseMapper<NoticeDTO, Notice> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "targetClass", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notice toEntity(NoticeDTO dto);

    @Override
    @Mapping(source = "targetClass.id", target = "classId")
    @Mapping(source = "targetClass.name", target = "className")
    NoticeDTO toDTO(Notice notice);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "targetClass", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(NoticeDTO dto, @MappingTarget Notice notice);
}
