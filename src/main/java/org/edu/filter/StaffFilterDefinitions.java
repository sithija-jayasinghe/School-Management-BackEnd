package org.edu.filter;

import org.edu.entity.Staff;
import org.edu.util.EmploymentType;
import org.edu.util.Role;
import org.edu.util.StaffCategory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StaffFilterDefinitions {

    private StaffFilterDefinitions() {
    }

    public static Map<String, FilterDefinition<Staff>> definitions() {
        Map<String, FilterDefinition<Staff>> definitions = new LinkedHashMap<>();

        definitions.put("active", FilterSpecifications.equalsBoolean("active"));
        definitions.put("category", FilterSpecifications.equalsEnum("staffCategory", StaffCategory.class));
        definitions.put("employmentType", FilterSpecifications.equalsEnum("employmentType", EmploymentType.class));
        definitions.put("department", FilterSpecifications.equalsIgnoreCase("department"));
        definitions.put("teachingCapable", FilterSpecifications.equalsBoolean("teachingCapable"));
        definitions.put("role", FilterSpecifications.equalsEnum("user.role", Role.class));
        definitions.put("keyword", keywordFilter());

        return Map.copyOf(definitions);
    }

    private static FilterDefinition<Staff> keywordFilter() {
        return rawValue -> FilterSpecifications.<Staff>likeIgnoreCase("name", rawValue)
                .or(FilterSpecifications.likeIgnoreCase("staffId", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("designation", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("department", rawValue))
                .or(FilterSpecifications.likeIgnoreCase("phoneNumber", rawValue));
    }
}
