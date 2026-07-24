package org.edu.mapper;

import org.mapstruct.MappingTarget;

public interface BaseMapper<D, E> {

    E toEntity(D dto);

    D toDTO(E entity);

    void updateEntityFromDTO(D dto, @MappingTarget E entity);
}
