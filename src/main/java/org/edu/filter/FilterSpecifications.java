package org.edu.filter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Map;

public final class FilterSpecifications {

    private FilterSpecifications() {
    }

    public static <T> Specification<T> build(Map<String, String> filters, Map<String, FilterDefinition<T>> definitions) {
        Specification<T> specification = Specification.where(null);

        if (filters == null || filters.isEmpty()) {
            return specification;
        }

        for (Map.Entry<String, String> filter : filters.entrySet()) {
            FilterDefinition<T> definition = definitions.get(filter.getKey());
            if (definition != null && hasText(filter.getValue())) {
                specification = specification.and(definition.toSpecification(filter.getValue().trim()));
            }
        }

        return specification;
    }

    public static <T> FilterDefinition<T> equalsLong(String fieldPath) {
        return rawValue -> {
            Long value = Long.valueOf(rawValue);
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(resolvePath(root, fieldPath), value);
        };
    }

    public static <T> FilterDefinition<T> equalsBoolean(String fieldPath) {
        return rawValue -> {
            Boolean value = Boolean.valueOf(rawValue);
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(resolvePath(root, fieldPath), value);
        };
    }

    public static <T, E extends Enum<E>> FilterDefinition<T> equalsEnum(String fieldPath, Class<E> enumClass) {
        return rawValue -> {
            E value = Enum.valueOf(enumClass, rawValue.trim().toUpperCase());
            return (root, query, criteriaBuilder) -> criteriaBuilder.equal(resolvePath(root, fieldPath), value);
        };
    }

    public static <T> FilterDefinition<T> equalsIgnoreCase(String fieldPath) {
        return rawValue -> (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(resolveStringPath(root, fieldPath)), rawValue.toLowerCase());
    }

    public static <T> Specification<T> likeIgnoreCase(String fieldPath, String keyword) {
        String pattern = "%" + keyword.toLowerCase() + "%";
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(resolveStringPath(root, fieldPath)), pattern);
    }

    public static <T> FilterDefinition<T> dateGreaterThanOrEqual(String fieldPath) {
        return rawValue -> {
            LocalDate value = LocalDate.parse(rawValue);
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(resolvePath(root, fieldPath), value);
        };
    }

    public static <T> FilterDefinition<T> dateLessThanOrEqual(String fieldPath) {
        return rawValue -> {
            LocalDate value = LocalDate.parse(rawValue);
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(resolvePath(root, fieldPath), value);
        };
    }

    public static <T> FilterDefinition<T> hasCollectionItems(String collectionPath) {
        return rawValue -> {
            boolean expected = Boolean.parseBoolean(rawValue);
            return (root, query, criteriaBuilder) -> {
                query.distinct(true);
                Join<?, ?> join = getOrCreateJoin(root, collectionPath);
                return expected
                        ? criteriaBuilder.isNotNull(join.get("id"))
                        : criteriaBuilder.isNull(join.get("id"));
            };
        };
    }

    @SuppressWarnings("unchecked")
    public static <T, V> Path<V> resolvePath(Root<T> root, String fieldPath) {
        String[] fields = fieldPath.split("\\.");
        Path<?> path = root;

        for (int index = 0; index < fields.length; index++) {
            if (index < fields.length - 1 && path instanceof Root<?> currentRoot) {
                path = getOrCreateJoin(currentRoot, fields[index]);
            } else if (index < fields.length - 1 && path instanceof Join<?, ?> currentJoin) {
                path = getOrCreateJoin(currentJoin, fields[index]);
            } else {
                path = path.get(fields[index]);
            }
        }

        return (Path<V>) path;
    }

    public static <T> Path<String> resolveStringPath(Root<T> root, String fieldPath) {
        return resolvePath(root, fieldPath);
    }

    private static Join<?, ?> getOrCreateJoin(Root<?> root, String field) {
        return root.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals(field))
                .findFirst()
                .orElseGet(() -> root.join(field, JoinType.LEFT));
    }

    private static Join<?, ?> getOrCreateJoin(Join<?, ?> parentJoin, String field) {
        return parentJoin.getJoins().stream()
                .filter(join -> join.getAttribute().getName().equals(field))
                .findFirst()
                .orElseGet(() -> parentJoin.join(field, JoinType.LEFT));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
