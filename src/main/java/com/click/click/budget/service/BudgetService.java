package com.click.click.budget.service;

import com.click.click.budget.entity.BudgetEntity;
import com.click.click.budget.repository.BudgetRepository;
import com.click.click.consumption.entity.CategoryEntity;
import com.click.click.consumption.repository.CategoryRepository;
import com.click.click.user.entity.UserEntity;
import com.click.click.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private UserEntity currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("인증 사용자 정보를 찾을 수 없습니다."));
    }

    private LocalDate normalizeToFirstDay(YearMonth ym) {
        return LocalDate.of(ym.getYear(), ym.getMonth(), 1);
    }

    @Transactional
    public BudgetEntity upsert(YearMonth ym, String categoryName, long amount) {
        UserEntity user = currentUser();
        LocalDate firstDay = normalizeToFirstDay(ym);

        CategoryEntity category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryName));

        BudgetEntity entity = budgetRepository
                .findByUserAndYearMonthAndCategory(user, firstDay, category)
                .orElse(BudgetEntity.builder()
                        .user(user)
                        .yearMonth(firstDay)
                        .category(category)
                        .amount(0L)
                        .build());

        entity.setAmount(amount);
        return budgetRepository.save(entity);
    }

    @Transactional
    public void delete(Integer budgetId) {
        UserEntity user = currentUser();
        BudgetEntity entity = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다: " + budgetId));

        // 본인 소유인지 검증
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        budgetRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<BudgetEntity> findByMonth(YearMonth ym) {
        UserEntity user = currentUser();
        LocalDate firstDay = normalizeToFirstDay(ym);
        return budgetRepository.findByUserAndYearMonth(user, firstDay);
    }

    @Transactional
    public BudgetEntity updateAmount(Integer budgetId, long amount) {
        UserEntity user = currentUser();
        BudgetEntity entity = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("예산을 찾을 수 없습니다: " + budgetId));
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        entity.setAmount(amount);
        return budgetRepository.save(entity);
    }
}