package org.edu.filter;

import org.edu.entity.Parent;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ParentFilterDefinitions {

    private ParentFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Parent>> definitions() {
        Map<String, FilterDefinition<Parent>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Parent> keywordFilter() {
        return rawValue -> FilterSpecifications.<Parent>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("phoneNumber", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("address", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("occupation", rawValue));
    }
}
