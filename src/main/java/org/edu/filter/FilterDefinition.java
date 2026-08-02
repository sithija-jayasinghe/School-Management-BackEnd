package org.edu.filter;

import org.springframework.data.jpa.domain.Specification;

@FunctionalInterface
public interface FilterDefinition<T> {

    Specification<T> toSpecification(String rawValue);
}
