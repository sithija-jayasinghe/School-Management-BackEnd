package org.edu.filter;

import org.edu.entity.Subject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SubjectFilterDefinitions {

    private SubjectFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Subject>> definitions() {
        Map<String, FilterDefinition<Subject>> definitions = new LinkedHashMap<>();

        definitions.put("gradeId", FilterSpecifications.equalsLong("grades.id"));
        definitions.put("hasClassCoverage", FilterSpecifications.hasCollectionItems("classes"));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Subject> keywordFilter() {
        return rawValue -> FilterSpecifications.<Subject>likeIgnoreCase("code", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("name", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("description", rawValue));
    }
}
