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
}