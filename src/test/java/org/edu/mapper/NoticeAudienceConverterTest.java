package org.edu.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.edu.util.NoticeAudience;
import org.junit.jupiter.api.Test;

class NoticeAudienceConverterTest {

    private final NoticeAudienceConverter converter = new NoticeAudienceConverter();

    @Test
    void shouldReadLegacyStudentsAudienceAsParents() {
        assertEquals(NoticeAudience.PARENTS, converter.convertToEntityAttribute("STUDENTS"));
    }

    @Test
    void shouldWriteCurrentAudienceNames() {
        assertEquals("CLASS", converter.convertToDatabaseColumn(NoticeAudience.CLASS));
    }
}
