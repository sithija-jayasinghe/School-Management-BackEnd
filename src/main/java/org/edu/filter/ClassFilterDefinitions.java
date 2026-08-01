package org.edu.filter;

import org.edu.entity.Class;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClassFilterDefinitions {

    private ClassFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Class>> definitions() {
        Map<String, FilterDefinition<Class>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("academicYearId", FilterSpecifications.equalsLong("academicYear.id"));
        definitions.put("gradeId", FilterSpecifications.equalsLong("grade.id"));
        definitions.put("classTeacherId", FilterSpecifications.equalsLong("classTeacher.id"));
        definitions.put("section", FilterSpecifications.equalsIgnoreCase("section"));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Class> keywordFilter() {
        return rawValue -> FilterSpecifications.<Class>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("section", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("grade.name", rawValue));
    }
}
