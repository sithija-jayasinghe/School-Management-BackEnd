package org.edu.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.NoticeDTO;
import org.edu.service.NoticeService;
import org.edu.util.NoticeAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public NoticeDTO createNotice(@Valid @RequestBody NoticeDTO dto) {
        return noticeService.createNotice(dto);
    }

    @PatchMapping("/{id}")
    public NoticeDTO updateNotice(@PathVariable Long id, @RequestBody NoticeDTO dto) {
        return noticeService.updateNotice(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
    }

    @PostMapping("/{id}/publish")
    public NoticeDTO publishNotice(@PathVariable Long id) {
        return noticeService.publishNotice(id);
    }

    @PostMapping("/{id}/unpublish")
    public NoticeDTO unpublishNotice(@PathVariable Long id) {
        return noticeService.unpublishNotice(id);
    }

    @GetMapping
    public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeService.getAllNotices(pageable);
    }

    @GetMapping("/{id}")
    public NoticeDTO getNoticeById(@PathVariable Long id) {
        return noticeService.getNoticeById(id);
    }

    @GetMapping("/search")
    public Page<NoticeDTO> searchNotices(@RequestParam String title, Pageable pageable) {
        return noticeService.searchNotices(title, pageable);
    }

    @GetMapping("/audience/{audience}")
    public Page<NoticeDTO> getNoticesByAudience(@PathVariable NoticeAudience audience, Pageable pageable) {
        return noticeService.getNoticesByAudience(audience, pageable);
    }

    @GetMapping("/classes/{classId}")
    public List<NoticeDTO> getClassNotices(@PathVariable Long classId) {
        return noticeService.getClassNotices(classId);
    }
}
