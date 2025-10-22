package com.click.click.consumption.repository;

import com.click.click.consumption.dto.ConsumptionSearchDTO;
import com.click.click.consumption.dto.ConsumptionSummaryDTO;
import com.click.click.consumption.entity.ConsumptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConsumptionRepository extends JpaRepository<ConsumptionEntity, Long> {

    /* 목록 페이지 */
    @Query(value = """
        select new com.click.click.consumption.dto.ConsumptionSearchDTO(
            e.id, e.date, e.amount, c.name, c.type
        )
        from ConsumptionEntity e
          join e.category c
        where e.user.id = :userId
          and e.date between :start and :end
          and (:category is null or c.name = :category)
        order by e.date desc, e.id desc
        """,
            countQuery = """
        select count(e)
        from ConsumptionEntity e
          join e.category c
        where e.user.id = :userId
          and e.date between :start and :end
          and (:category is null or c.name = :category)
        """)
    Page<ConsumptionSearchDTO> findPageByUserAndRange(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("category") String category,
            Pageable pageable
    );

    /* 카테고리 분포 */
    @Query("""
        select new com.click.click.consumption.dto.ConsumptionSummaryDTO(
            c.name, sum(e.amount)
        )
        from ConsumptionEntity e
          join e.category c
        where e.user.id = :userId
          and e.date between :start and :end
          and (:category is null or c.name = :category)
        group by c.name
        order by sum(e.amount) desc
        """)
    List<ConsumptionSummaryDTO> summarizeByCategory(
            @Param("userId") Long userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("category") String category
    );

    /* ✅ 월 합계/건수 */
    @Query("""
        select coalesce(sum(e.amount),0)
        from ConsumptionEntity e
        where e.user.id = :userId
          and e.date between :start and :end
        """)
    Long sumAmountByUserAndRange(@Param("userId") Long userId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);

    @Query("""
        select count(e)
        from ConsumptionEntity e
        where e.user.id = :userId
          and e.date between :start and :end
        """)
    long countByUserAndRange(@Param("userId") Long userId,
                             @Param("start") LocalDate start,
                             @Param("end") LocalDate end);

    /* ✅ 업로드 중복 방지 체크 */
    boolean existsByUser_IdAndDateAndAmountAndCategory_Id(Long userId, LocalDate date, Long amount, Integer categoryId);
}
