package org.edu.mapper;

import org.edu.dto.DocumentDTO;
import org.edu.entity.Document;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper extends BaseMapper<DocumentDTO, Document> {

    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "storedFileName", ignore = true)
    @Mapping(target = "originalFileName", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Document toEntity(DocumentDTO dto);

    @Override
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    @Mapping(source = "student.currentClass.id", target = "classId")
    @Mapping(source = "student.currentClass.name", target = "className")
    @Mapping(source = "uploadedBy.id", target = "uploadedByUserId")
    @Mapping(source = "uploadedBy.name", target = "uploadedByUserName")
    @Mapping(source = "uploadedBy.role", target = "uploadedByRole")
    DocumentDTO toDTO(Document document);

    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "storedFileName", ignore = true)
    @Mapping(target = "originalFileName", ignore = true)
    @Mapping(target = "contentType", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(DocumentDTO dto, @MappingTarget Document entity);
}
