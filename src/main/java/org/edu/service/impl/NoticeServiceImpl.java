package org.edu.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.NoticeDTO;
import org.edu.entity.Notice;
import org.edu.exception.ResourceNotFoundException;
import org.edu.mapper.NoticeMapper;
import org.edu.repository.ClassRepository;
import org.edu.repository.NoticeRepository;
import org.edu.service.NoticeService;
import org.edu.util.NoticeAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final ClassRepository classRepository;
    private final NoticeMapper noticeMapper;

    @Override
    public NoticeDTO createNotice(NoticeDTO dto) {
        validateNoticeDates(dto);
        Notice notice = noticeMapper.toEntity(dto);
        notice.setActive(true);
        applyTargetClass(notice, dto);
        return noticeMapper.toDTO(noticeRepository.save(notice));
    }

    @Override
    public NoticeDTO updateNotice(Long id, NoticeDTO dto) {
        Notice notice = getNotice(id);
        NoticeDTO resolved = resolveForUpdate(dto, notice);
        validateNoticeDates(resolved);

        noticeMapper.updateEntityFromDTO(dto, notice);
        applyTargetClass(notice, resolved);
        return noticeMapper.toDTO(noticeRepository.save(notice));
    }

    @Override
    public void deleteNotice(Long id) {
        Notice notice = getNotice(id);
        if (!notice.isActive()) {
            throw new IllegalStateException("Notice already inactive");
        }
        notice.setActive(false);
    }

    @Override
    public NoticeDTO publishNotice(Long id) {
        Notice notice = getNotice(id);
        if (notice.isPublished()) {
            throw new IllegalStateException("Notice already published");
        }
        notice.setPublished(true);
        if (notice.getPublishDate() == null) {
            notice.setPublishDate(LocalDate.now());
        }
        return noticeMapper.toDTO(notice);
    }

    @Override
    public NoticeDTO unpublishNotice(Long id) {
        Notice notice = getNotice(id);
        if (!notice.isPublished()) {
            throw new IllegalStateException("Notice already unpublished");
        }
        notice.setPublished(false);
        return noticeMapper.toDTO(notice);
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDTO getNoticeById(Long id) {
        return noticeMapper.toDTO(getNotice(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeRepository.findByActiveTrue(pageable)
                .map(noticeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> searchNotices(String title, Pageable pageable) {
        return noticeRepository.findByTitleContainingIgnoreCaseAndActiveTrue(title, pageable)
                .map(noticeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getNoticesByAudience(NoticeAudience audience, Pageable pageable) {
        return noticeRepository.findByAudienceAndActiveTrue(audience, pageable)
                .map(noticeMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getClassNotices(Long classId) {
        return noticeRepository.findByTargetClassIdAndActiveTrue(classId)
                .stream()
                .map(noticeMapper::toDTO)
                .toList();
    }

    private Notice getNotice(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notice not found with id: " + id));
    }

    private void applyTargetClass(Notice notice, NoticeDTO dto) {
        if (dto.getAudience() != NoticeAudience.CLASS) {
            notice.setTargetClass(null);
            return;
        }

        if (dto.getClassId() == null) {
            throw new IllegalArgumentException("Class ID is required for class notices");
        }

        org.edu.entity.Class targetClass = classRepository.findByIdAndActiveTrue(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + dto.getClassId()));
        notice.setTargetClass(targetClass);
    }

    private void validateNoticeDates(NoticeDTO dto) {
        if (dto.getPublishDate() != null && dto.getExpiryDate() != null && dto.getPublishDate().isAfter(dto.getExpiryDate())) {
            throw new IllegalArgumentException("Publish date must be before or equal to expiry date");
        }
    }

    private NoticeDTO resolveForUpdate(NoticeDTO dto, Notice notice) {
        NoticeDTO resolved = new NoticeDTO();
        resolved.setTitle(dto.getTitle() == null ? notice.getTitle() : dto.getTitle());
        resolved.setMessage(dto.getMessage() == null ? notice.getMessage() : dto.getMessage());
        resolved.setAudience(dto.getAudience() == null ? notice.getAudience() : dto.getAudience());
        resolved.setClassId(dto.getClassId() == null && notice.getTargetClass() != null
                ? notice.getTargetClass().getId()
                : dto.getClassId());
        resolved.setPublishDate(dto.getPublishDate() == null ? notice.getPublishDate() : dto.getPublishDate());
        resolved.setExpiryDate(dto.getExpiryDate() == null ? notice.getExpiryDate() : dto.getExpiryDate());
        return resolved;
    }
}
