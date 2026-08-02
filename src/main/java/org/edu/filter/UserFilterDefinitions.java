package org.edu.filter;

import org.edu.entity.User;
import org.edu.util.Role;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UserFilterDefinitions {

    private UserFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<User>> definitions() {
        Map<String, FilterDefinition<User>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("role", FilterSpecifications.equalsEnum("role", Role.class));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<User> keywordFilter() {
        return rawValue -> FilterSpecifications.<User>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("email", rawValue));
    }
}
