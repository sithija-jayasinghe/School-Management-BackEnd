package org.edu.filter;

import org.edu.entity.AcademicYear;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AcademicYearFilterDefinitions {

    private AcademicYearFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<AcademicYear>> definitions() {
        Map<String, FilterDefinition<AcademicYear>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("current", FilterSpecifications.equalsBoolean("current"));
        definitions.put("from", FilterSpecifications.dateGreaterThanOrEqual("startDate"));
        definitions.put("to", FilterSpecifications.dateLessThanOrEqual("endDate"));
        definitions.put("keyword", rawValue -> FilterSpecifications.likeIgnoreCase("name", rawValue));

        return Map.copyOf(definitions);
    }
}
