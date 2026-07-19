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

    @Query("""
            select distinct n
            from Notice n
            left join fetch n.targetClass targetClass
            where n.active = true
              and n.published = true
              and (n.publishDate is null or n.publishDate <= :today)
              and (n.expiryDate is null or n.expiryDate >= :today)
              and (
                    n.audience = org.edu.util.NoticeAudience.ALL
                 or n.audience = org.edu.util.NoticeAudience.PARENTS
                 or (n.audience = org.edu.util.NoticeAudience.CLASS and targetClass.id in :classIds)
              )
            order by n.publishDate desc, n.createdAt desc
            """)
    List<Notice> findParentPortalNotices(
            @Param("classIds") List<Long> classIds,
            @Param("today") LocalDate today
    );

    @Query("""
            select distinct n
            from Notice n
            left join fetch n.targetClass targetClass
            where n.active = true
              and n.published = true
              and (n.publishDate is null or n.publishDate <= :today)
              and (n.expiryDate is null or n.expiryDate >= :today)
              and (
                    n.audience = org.edu.util.NoticeAudience.ALL
                 or n.audience = org.edu.util.NoticeAudience.STUDENTS
                 or (n.audience = org.edu.util.NoticeAudience.CLASS and targetClass.id = :classId)
              )
            order by n.publishDate desc, n.createdAt desc
            """)
    List<Notice> findStudentPortalNotices(
            @Param("classId") Long classId,
            @Param("today") LocalDate today
    );

    @Query("""
            select n
            from Notice n
            left join fetch n.targetClass
            where n.active = true
              and n.published = true
              and (n.publishDate is null or n.publishDate <= :today)
              and (n.expiryDate is null or n.expiryDate >= :today)
              and (n.audience = org.edu.util.NoticeAudience.ALL or n.audience = :audience)
            order by n.publishDate desc, n.createdAt desc
            """)
    List<Notice> findPortalNotices(
            @Param("audience") NoticeAudience audience,
            @Param("today") LocalDate today
    );
}
