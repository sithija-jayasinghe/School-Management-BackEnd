package org.edu.dto.parentportal;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.edu.util.NoticeAudience;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentPortalNoticeDTO {

    private Long noticeId;
    private String title;
    private String message;
    private NoticeAudience audience;
    private Long classId;
    private String className;
    private LocalDate publishDate;
    private LocalDate expiryDate;
}
