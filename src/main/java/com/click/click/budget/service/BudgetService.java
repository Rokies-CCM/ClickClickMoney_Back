package com.click.click.budget.service;

import com.click.click.budget.entity.BudgetEntity;
import com.click.click.budget.repository.BudgetRepository;
import com.click.click.consumption.entity.CategoryEntity;
import com.click.click.consumption.repository.CategoryRepository;
import com.click.click.user.entity.UserEntity;
import com.click.click.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("인증 사용자 정보를 찾을 수 없습니다."));
    }

    private LocalDate normalizeToFirstDay(YearMonth ym) {
        return ym.atDay(1);
    }

    @Transactional
    public BudgetEntity upsert(YearMonth ym, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("예산 금액은 음수가 될 수 없습니다.");
        }

        UserEntity user = currentUser();
        LocalDate firstDay = normalizeToFirstDay(ym);

        BudgetEntity entity = budgetRepository
                .findByUserAndBudgetMonth(user, firstDay )
                .orElseGet(() -> BudgetEntity.builder()
                        .user(user)
                        .budgetMonth(firstDay)
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

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        budgetRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<BudgetEntity> findByMonth(YearMonth ym) {
        UserEntity user = currentUser();
        LocalDate firstDay = normalizeToFirstDay(ym);
        return budgetRepository.findAllByUserAndBudgetMonth(user, firstDay);
    }

    @Transactional
    public BudgetEntity updateAmount(Integer budgetId, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("예산 금액은 음수가 될 수 없습니다.");
        }
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