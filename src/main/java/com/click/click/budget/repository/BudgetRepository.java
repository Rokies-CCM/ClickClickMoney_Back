package com.click.click.budget.repository;

import com.click.click.budget.entity.BudgetEntity;
import com.click.click.consumption.entity.CategoryEntity;
import com.click.click.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Integer> {

    Optional<BudgetEntity> findByUserAndBudgetMonth(UserEntity user, LocalDate budgetMonth);

    List<BudgetEntity> findAllByUserAndBudgetMonth(UserEntity user, LocalDate budgetMonth);
}