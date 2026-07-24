package org.edu.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.edu.dto.NoticeDTO;
import org.edu.service.AuditLogService;
import org.edu.service.NoticeService;
import org.edu.util.AuditAction;
import org.edu.util.AuditEntityType;
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
@Tag(name = "Notices", description = "Create, publish, and filter school notices")
@SecurityRequirement(name = "bearerAuth")
public class NoticeController {

    private final NoticeService noticeService;
    private final AuditLogService auditLogService;

    @PostMapping
    @Operation(summary = "Create a notice")
    public NoticeDTO createNotice(@Valid @RequestBody NoticeDTO dto) {
        NoticeDTO response = noticeService.createNotice(dto);
        auditLogService.log(AuditAction.CREATE, AuditEntityType.NOTICE, response.getId(), response.getTitle(), "Created notice");
        return response;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a notice")
    public NoticeDTO updateNotice(@PathVariable Long id, @Valid @RequestBody NoticeDTO dto) {
        NoticeDTO response = noticeService.updateNotice(id, dto);
        auditLogService.log(AuditAction.UPDATE, AuditEntityType.NOTICE, response.getId(), response.getTitle(), "Updated notice");
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a notice")
    public void deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        auditLogService.log(AuditAction.DEACTIVATE, AuditEntityType.NOTICE, id, "Notice #" + id, "Deactivated notice");
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a notice")
    public NoticeDTO publishNotice(@PathVariable Long id) {
        NoticeDTO response = noticeService.publishNotice(id);
        auditLogService.log(AuditAction.PUBLISH, AuditEntityType.NOTICE, response.getId(), response.getTitle(), "Published notice");
        return response;
    }

    @PostMapping("/{id}/unpublish")
    @Operation(summary = "Unpublish a notice")
    public NoticeDTO unpublishNotice(@PathVariable Long id) {
        NoticeDTO response = noticeService.unpublishNotice(id);
        auditLogService.log(AuditAction.UNPUBLISH, AuditEntityType.NOTICE, response.getId(), response.getTitle(), "Unpublished notice");
        return response;
    }

    @GetMapping
    @Operation(summary = "List notices")
    public Page<NoticeDTO> getAllNotices(Pageable pageable) {
        return noticeService.getAllNotices(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notice by id")
    public NoticeDTO getNoticeById(@PathVariable Long id) {
        return noticeService.getNoticeById(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search notices by title")
    public Page<NoticeDTO> searchNotices(@RequestParam String title, Pageable pageable) {
        return noticeService.searchNotices(title, pageable);
    }

    @GetMapping("/audience/{audience}")
    @Operation(summary = "List notices by audience")
    public Page<NoticeDTO> getNoticesByAudience(@PathVariable NoticeAudience audience, Pageable pageable) {
        return noticeService.getNoticesByAudience(audience, pageable);
    }

    @GetMapping("/classes/{classId}")
    @Operation(summary = "List notices targeted to a class")
    public List<NoticeDTO> getClassNotices(@PathVariable Long classId) {
        return noticeService.getClassNotices(classId);
    }
}
