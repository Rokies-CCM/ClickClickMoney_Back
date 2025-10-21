package com.click.click.consumption.service;

import com.click.click.consumption.dto.ConsumptionDTO;
import com.click.click.consumption.dto.ConsumptionSearchDTO;
import com.click.click.consumption.dto.ConsumptionSummaryDTO;
import com.click.click.consumption.entity.CategoryEntity;
import com.click.click.consumption.entity.ConsumptionEntity;
import com.click.click.user.entity.UserEntity;
import com.click.click.consumption.repository.CategoryRepository;
import com.click.click.consumption.repository.ConsumptionRepository;
import com.click.click.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ConsumptionRepository consumptionRepository;


    @Transactional
    public void record(ConsumptionDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("인증 사용자 정보를 찾을 수 없습니다."));

        for (ConsumptionDTO.Item item : dto.getItems()) {
            CategoryEntity category = categoryRepository.findByName(item.getCategory())
                    .orElseGet(() -> categoryRepository.save(
                            CategoryEntity.builder()
                                    .name(item.getCategory())
                                    .type("기타")
                                    .build()
                    ));

            long amount = toLongExact(item.getAmount());

            ConsumptionEntity entity = ConsumptionEntity.builder()
                    .user(user)
                    .category(category)
                    .date(item.getDate())
                    .amount(amount)
                    .build();
            consumptionRepository.save(entity);
        }
    }

    @Transactional(readOnly = true)
    public Page<ConsumptionSearchDTO> findPage(LocalDate start, LocalDate end, String category, int page, int size) {
        UserEntity user = currentUser();

        LocalDate to = (end != null) ? end : LocalDate.now();
        LocalDate from = (start != null) ? start : to.minusDays(29);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        String normalized = (category == null || category.isBlank()) ? null : category.trim();

        return consumptionRepository.findPageByUserAndRange(
                user.getId(), from, to, normalized, pageable
        );
    }

    @Transactional(readOnly = true)
    public List<ConsumptionSummaryDTO> summarize(LocalDate start, LocalDate end, String category) {
        UserEntity user = currentUser();

        LocalDate to = (end != null) ? end : LocalDate.now();
        LocalDate from = (start != null) ? start : to.minusDays(29);
        String normalized = (category == null || category.isBlank()) ? null : category.trim();

        return consumptionRepository.summarizeByCategory(
                user.getId(), from, to, normalized
        );
    }

    private long toLongExact(java.math.BigInteger v) {
        try {
            return v.longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("금액이 너무 큽니다");
        }
    }

    private UserEntity currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("인증 사용자 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public void update(Long id, LocalDate date, String category, Long amount) {
        UserEntity user = currentUser();
        ConsumptionEntity entity = consumptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소비 내역입니다. id=" + id));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 소비 내역에 대한 수정 권한이 없습니다.");
        }

        if (date != null) {
            entity.setDate(date);
        }
        if (category != null && !category.isBlank()) {
            String name = category.trim();
            CategoryEntity cat = categoryRepository.findByName(name)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. name=" + name));
            entity.setCategory(cat);
        }
        if (amount != null) {
            if (amount < 0) throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
            entity.setAmount(amount);
        }
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = currentUser();

        ConsumptionEntity entity = consumptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 소비 내역입니다. id=" + id));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("해당 소비 내역에 대한 삭제 권한이 없습니다.");
        }

        consumptionRepository.delete(entity);
    }
}