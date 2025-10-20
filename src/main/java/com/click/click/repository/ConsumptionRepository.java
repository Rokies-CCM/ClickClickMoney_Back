package com.click.click.repository;

import com.click.click.entity.ConsumptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumptionRepository extends JpaRepository<ConsumptionEntity, Long> {
}