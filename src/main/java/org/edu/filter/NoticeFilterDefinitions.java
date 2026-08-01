package org.edu.filter;

import org.edu.entity.Notice;
import org.edu.util.NoticeAudience;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NoticeFilterDefinitions {

    private NoticeFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Notice>> definitions() {
        Map<String, FilterDefinition<Notice>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("published", FilterSpecifications.equalsBoolean("published"));
        definitions.put("audience", FilterSpecifications.equalsEnum("audience", NoticeAudience.class));
        definitions.put("classId", FilterSpecifications.equalsLong("targetClass.id"));
        definitions.put("from", FilterSpecifications.dateGreaterThanOrEqual("publishDate"));
        definitions.put("to", FilterSpecifications.dateLessThanOrEqual("expiryDate"));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Notice> keywordFilter() {
        return rawValue -> FilterSpecifications.<Notice>likeIgnoreCase("title", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("message", rawValue));
    }
}
