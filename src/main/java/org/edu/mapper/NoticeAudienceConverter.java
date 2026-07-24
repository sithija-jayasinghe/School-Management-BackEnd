package org.edu.mapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.edu.util.NoticeAudience;

@Converter
public class NoticeAudienceConverter implements AttributeConverter<NoticeAudience, String> {

    @Override
    public String convertToDatabaseColumn(NoticeAudience audience) {
        return audience == null ? null : audience.name();
    }

    @Override
    public NoticeAudience convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();
        if ("STUDENTS".equals(normalized)) {
            return NoticeAudience.PARENTS;
        }

        return NoticeAudience.valueOf(normalized);
    }
}
