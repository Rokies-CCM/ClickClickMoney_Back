package com.click.click.service;

import com.click.click.dto.ConsumptionDTO;
import com.click.click.entity.CategoryEntity;
import com.click.click.entity.ConsumptionEntity;
import com.click.click.entity.UserEntity;
import com.click.click.repository.CategoryRepository;
import com.click.click.repository.ConsumptionRepository;
import com.click.click.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                            CategoryEntity.builder().name(item.getCategory()).build())
                    );

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

    private long toLongExact(java.math.BigInteger v) {
        try {
            return v.longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("금액이 너무 큽니다");
        }
    }
}
