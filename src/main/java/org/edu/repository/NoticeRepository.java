package org.edu.repository;

import java.time.LocalDate;
import java.util.List;
import org.edu.entity.Notice;
import org.edu.util.NoticeAudience;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByActiveTrue(Pageable pageable);

    Page<Notice> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);

    Page<Notice> findByAudienceAndActiveTrue(NoticeAudience audience, Pageable pageable);

    List<Notice> findByTargetClassIdAndActiveTrue(Long classId);

    @Query("""
            select n
            from Notice n
            left join fetch n.targetClass
            where n.active = true
              and n.published = true
              and (n.publishDate is null or n.publishDate <= :today)
              and (n.expiryDate is null or n.expiryDate >= :today)
            order by n.publishDate desc, n.createdAt desc
            """)
    List<Notice> findPublishedVisibleNotices(@Param("today") LocalDate today);
}
