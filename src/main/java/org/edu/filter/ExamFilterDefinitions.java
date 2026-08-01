package org.edu.filter;

import org.edu.entity.Exam;
import org.edu.util.ExamType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExamFilterDefinitions {

    private ExamFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Exam>> definitions() {
        Map<String, FilterDefinition<Exam>> definitions = new LinkedHashMap<>();

        definitions.put("academicYearId", FilterSpecifications.equalsLong("academicYear.id"));
        definitions.put("academicTermId", FilterSpecifications.equalsLong("academicTerm.id"));
        definitions.put("classId", FilterSpecifications.equalsLong("studentClass.id"));
        definitions.put("subjectId", FilterSpecifications.equalsLong("subject.id"));
        definitions.put("type", FilterSpecifications.equalsEnum("type", ExamType.class));
        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("from", FilterSpecifications.dateGreaterThanOrEqual("examDate"));
        definitions.put("to", FilterSpecifications.dateLessThanOrEqual("examDate"));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Exam> keywordFilter() {
        return rawValue -> FilterSpecifications.<Exam>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("subject.name", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("studentClass.name", rawValue));
    }
}
