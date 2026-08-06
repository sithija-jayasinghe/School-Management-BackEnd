package org.edu.filter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.edu.entity.House;
import org.edu.entity.Activity;
import org.edu.entity.Student;
import org.edu.repository.HouseRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StudentFilterDefinitions {

    private StudentFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Student>> definitions(HouseRepository houseRepository) {
        Map<String, FilterDefinition<Student>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("classId", FilterSpecifications.equalsLong("currentClass.id"));
        definitions.put("gender", FilterSpecifications.equalsIgnoreCase("gender"));
        definitions.put("houseId", houseIdFilter(houseRepository));
        definitions.put("activityIds", activityIdsFilter());
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Student> houseIdFilter(HouseRepository houseRepository) {
        return rawValue -> {
            Long houseId = Long.valueOf(rawValue);
            String houseName = houseRepository.findByIdAndActiveTrue(houseId)
                    .map(House::getName)
                    .orElse(null);

            return (root, query, criteriaBuilder) -> {
                Specification<Student> assignedHouseMatch = (studentRoot, studentQuery, studentCriteriaBuilder) ->
                        studentCriteriaBuilder.equal(FilterSpecifications.resolvePath(studentRoot, "assignedHouse.id"), houseId);

                if (houseName == null) {
                    return assignedHouseMatch.toPredicate(root, query, criteriaBuilder);
                }

                Specification<Student> legacyHouseMatch = FilterSpecifications
                        .<Student>likeIgnoreCase("house", houseName)
                        .or(assignedHouseMatch);
                return legacyHouseMatch.toPredicate(root, query, criteriaBuilder);
            };
        };
    }

    private static FilterDefinition<Student> keywordFilter() {
        return rawValue -> FilterSpecifications.<Student>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("admissionNumber", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("nameWithInitials", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("house", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("assignedHouse.name", rawValue))
                .or((root, query, criteriaBuilder) ->
                        criteriaBuilder.like(root.get("id").as(String.class), "%" + rawValue + "%"));
    }

    private static FilterDefinition<Student> activityIdsFilter() {
        return rawValue -> {
            List<Long> activityIds = Arrays.stream(rawValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(Long::valueOf)
                    .toList();

            return (root, query, criteriaBuilder) -> {
                query.distinct(true);
                Join<Student, Activity> activityJoin = root.join("activities", JoinType.INNER);
                return activityJoin.get("id").in(activityIds);
            };
        };
    }
}
