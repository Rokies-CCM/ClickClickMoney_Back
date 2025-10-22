package com.click.click.consumption.repository;

import com.click.click.consumption.entity.MemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemoRepository extends JpaRepository<MemoEntity, Long> {
    List<MemoEntity> findByConsumption_IdOrderByIdDesc(Long consumptionId);
}
