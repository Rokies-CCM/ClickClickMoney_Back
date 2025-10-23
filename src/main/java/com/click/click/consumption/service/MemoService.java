package com.click.click.consumption.service;

import com.click.click.consumption.dto.MemoDTO;
import com.click.click.consumption.entity.ConsumptionEntity;
import com.click.click.consumption.entity.MemoEntity;
import com.click.click.consumption.repository.ConsumptionRepository;
import com.click.click.consumption.repository.MemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemoService {

    private final MemoRepository memoRepository;
    private final ConsumptionRepository consumptionRepository;

    @Transactional
    public MemoDTO.Response addMemo(Long consumptionId, String value) {
        ConsumptionEntity consumption = consumptionRepository.findById(consumptionId)
                .orElseThrow(() -> new IllegalArgumentException("소비내역이 존재하지 않습니다."));

        MemoEntity memo = MemoEntity.builder()
                .consumption(consumption)
                .value(value)
                .build();

        memoRepository.save(memo);

        return new MemoDTO.Response(memo.getId(), consumption.getId(), memo.getValue());
    }

    @Transactional(readOnly = true)
    public List<MemoDTO.Response> listMemos(Long consumptionId) {
        return memoRepository.findByConsumption_IdOrderByIdDesc(consumptionId)
                .stream()
                .map(m -> new MemoDTO.Response(m.getId(), consumptionId, m.getValue()))
                .toList();
    }

    @Transactional
    public MemoDTO.Response updateMemo(Long memoId, String value) {
        MemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 존재하지 않습니다."));

        memo.setValue(value);
        memoRepository.save(memo);

        return new MemoDTO.Response(memo.getId(), memo.getConsumption().getId(), memo.getValue());
    }

    @Transactional
    public void deleteMemo(Long memoId) {
        MemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new IllegalArgumentException("메모가 존재하지 않습니다."));
        memoRepository.delete(memo);
    }
}