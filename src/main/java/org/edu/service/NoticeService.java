package org.edu.service;

import java.util.List;
import org.edu.dto.NoticeDTO;
import org.edu.util.NoticeAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeService {

    NoticeDTO createNotice(NoticeDTO dto);

    NoticeDTO updateNotice(Long id, NoticeDTO dto);

    void deleteNotice(Long id);

    NoticeDTO publishNotice(Long id);

    NoticeDTO unpublishNotice(Long id);

    NoticeDTO getNoticeById(Long id);

    Page<NoticeDTO> getAllNotices(Pageable pageable);

    Page<NoticeDTO> searchNotices(String title, Pageable pageable);

    Page<NoticeDTO> getNoticesByAudience(NoticeAudience audience, Pageable pageable);

    List<NoticeDTO> getClassNotices(Long classId);
}
