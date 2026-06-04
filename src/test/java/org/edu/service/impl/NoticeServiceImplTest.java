package org.edu.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.edu.dto.NoticeDTO;
import org.edu.entity.Notice;
import org.edu.mapper.NoticeMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.NoticeRepository;
import org.edu.util.NoticeAudience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private ClassRepository classRepository;

    private NoticeServiceImpl noticeService;

    @BeforeEach
    void setUp() {
        NoticeMapper noticeMapper = Mappers.getMapper(NoticeMapper.class);
        noticeService = new NoticeServiceImpl(noticeRepository, classRepository, noticeMapper);
    }

    @Test
    void shouldCreateClassNoticeWithTargetClass() {
        NoticeDTO dto = noticeRequest(NoticeAudience.CLASS);
        dto.setClassId(5L);

        when(classRepository.findByIdAndActiveTrue(5L)).thenReturn(Optional.of(studentClass()));
        when(noticeRepository.save(org.mockito.Mockito.any(Notice.class)))
                .thenAnswer(invocation -> {
                    Notice notice = invocation.getArgument(0);
                    notice.setId(10L);
                    return notice;
                });

        NoticeDTO saved = noticeService.createNotice(dto);

        assertEquals(10L, saved.getId());
        assertEquals(5L, saved.getClassId());
        assertEquals("Grade 10A", saved.getClassName());
        assertTrue(saved.isActive());
    }

    @Test
    void shouldRejectClassNoticeWithoutClassId() {
        NoticeDTO dto = noticeRequest(NoticeAudience.CLASS);

        assertThrows(IllegalArgumentException.class, () -> noticeService.createNotice(dto));
        verify(noticeRepository, never()).save(org.mockito.Mockito.any(Notice.class));
    }

    @Test
    void shouldClearTargetClassForNonClassNotice() {
        NoticeDTO dto = noticeRequest(NoticeAudience.PARENTS);
        dto.setClassId(5L);

        when(noticeRepository.save(org.mockito.Mockito.any(Notice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NoticeDTO saved = noticeService.createNotice(dto);

        assertNull(saved.getClassId());
    }

    @Test
    void shouldRejectPublishDateAfterExpiryDate() {
        NoticeDTO dto = noticeRequest(NoticeAudience.ALL);
        dto.setPublishDate(LocalDate.of(2026, 2, 1));
        dto.setExpiryDate(LocalDate.of(2026, 1, 31));

        assertThrows(IllegalArgumentException.class, () -> noticeService.createNotice(dto));
    }

    @Test
    void shouldPublishUnpublishedNotice() {
        Notice notice = notice(NoticeAudience.ALL);
        notice.setPublished(false);

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        NoticeDTO published = noticeService.publishNotice(1L);

        assertTrue(published.isPublished());
    }

    @Test
    void shouldUnpublishPublishedNotice() {
        Notice notice = notice(NoticeAudience.ALL);
        notice.setPublished(true);

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        NoticeDTO unpublished = noticeService.unpublishNotice(1L);

        assertFalse(unpublished.isPublished());
    }

    @Test
    void shouldSoftDeleteActiveNotice() {
        Notice notice = notice(NoticeAudience.ALL);
        notice.setActive(true);

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        noticeService.deleteNotice(1L);

        assertFalse(notice.isActive());
    }

    private NoticeDTO noticeRequest(NoticeAudience audience) {
        NoticeDTO dto = new NoticeDTO();
        dto.setTitle("School Meeting");
        dto.setMessage("Parents meeting on Friday");
        dto.setAudience(audience);
        dto.setPublishDate(LocalDate.of(2026, 1, 1));
        dto.setExpiryDate(LocalDate.of(2026, 1, 31));
        return dto;
    }

    private Notice notice(NoticeAudience audience) {
        Notice notice = new Notice();
        notice.setId(1L);
        notice.setTitle("School Meeting");
        notice.setMessage("Parents meeting on Friday");
        notice.setAudience(audience);
        notice.setActive(true);
        return notice;
    }

    private org.edu.entity.Class studentClass() {
        org.edu.entity.Class studentClass = new org.edu.entity.Class();
        studentClass.setId(5L);
        studentClass.setName("Grade 10A");
        studentClass.setActive(true);
        return studentClass;
    }
}
