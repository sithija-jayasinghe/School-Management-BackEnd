package org.edu.mapper;

import lombok.RequiredArgsConstructor;
import org.edu.dto.ClassDTO;
import org.edu.entity.Class;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClassMapper {

    public Class toEntity(ClassDTO dto) {
        if (dto == null) {
            return null;
        }

        Class clazz = new Class();

        clazz.setName(dto.getName());
        clazz.setActive(dto.isActive());
        return clazz;
    }

    public ClassDTO toDTO(Class clazz) {
        if (clazz == null) {
            return null;
        }

        ClassDTO dto = new ClassDTO();

        dto.setId(clazz.getId());
        dto.setName(clazz.getName());
        dto.setActive(clazz.isActive());
        dto.setCreatedAt(clazz.getCreatedAt());
        dto.setUpdatedAt(clazz.getUpdatedAt());

        return dto;
    }

    public void updateEntityFromDTO(ClassDTO dto, Class clazz) {
        if (dto == null || clazz == null) {
            return;
        }

        if (dto.getName() != null) {
            clazz.setName(dto.getName());
        }
    }
}
