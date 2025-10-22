package com.click.click.consumption.service;

import com.click.click.consumption.dto.*;
import com.click.click.consumption.entity.CategoryEntity;
import com.click.click.consumption.entity.ConsumptionEntity;
import com.click.click.consumption.repository.CategoryRepository;
import com.click.click.consumption.repository.ConsumptionRepository;
import com.click.click.user.entity.UserEntity;
import com.click.click.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ConsumptionRepository consumptionRepository;

    /* ======================== 단건/배열 저장(기존) ======================== */
    @Transactional
    public void record(ConsumptionDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 사용자 정보를 찾을 수 없습니다."));

        for (ConsumptionDTO.Item item : dto.getItems()) {
            CategoryEntity category = categoryRepository.findByName(item.getCategory())
                    .orElseGet(() -> categoryRepository.save(
                            CategoryEntity.builder()
                                    .name(item.getCategory())
                                    .type("기타")
                                    .build()
                    ));

            long amount = toLongExact(item.getAmount());

            // 중복 방지: 같은 사용자/날짜/금액/카테고리 중복 삽입 방지
            if (consumptionRepository.existsByUser_IdAndDateAndAmountAndCategory_Id(
                    user.getId(), item.getDate(), amount, category.getId())) {
                continue;
            }

            ConsumptionEntity entity = ConsumptionEntity.builder()
                    .user(user)
                    .category(category)
                    .date(item.getDate())
                    .amount(amount)
                    .build();
            consumptionRepository.save(entity);
        }
    }

    /* ======================== 조회 ======================== */
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

    @Transactional(readOnly = true)
    public MonthlyDashboardDTO getMonthlyDashboard(LocalDate from, LocalDate to) {
        UserEntity user = currentUser();

        Long totalAmount = Optional.ofNullable(
                consumptionRepository.sumAmountByUserAndRange(user.getId(), from, to)
        ).orElse(0L);

        long totalCount = consumptionRepository.countByUserAndRange(user.getId(), from, to);

        List<ConsumptionSummaryDTO> breakdown =
                consumptionRepository.summarizeByCategory(user.getId(), from, to, null);

        return MonthlyDashboardDTO.builder()
                .from(from)
                .to(to)
                .yearMonth(String.format("%d-%02d", from.getYear(), from.getMonthValue()))
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .byCategory(breakdown)
                .build();
    }

    /* ======================== CSV 업로드 ======================== */
    @Transactional
    public ConsumptionImportResultDTO importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "파일이 비어 있습니다.");
        }

        UserEntity user = currentUser();

        int inserted = 0;
        int skipped = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(reader);

            Map<String, Integer> headerMap = parser.getHeaderMap();

            String hDate = findHeader(headerMap, "date", "날짜", "거래일자");
            String hCategory = findHeader(headerMap, "category", "카테고리", "분류");
            String hAmount = findHeader(headerMap, "amount", "금액", "지출", "사용금액");

            if (hDate == null || hCategory == null || hAmount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "CSV 헤더를 인식할 수 없습니다. (필수: date/날짜, category/카테고리, amount/금액)");
            }

            for (CSVRecord rec : parser) {
                try {
                    String catName = rec.get(hCategory).trim();
                    String rawAmount = rec.get(hAmount).trim();
                    String rawDate = rec.get(hDate).trim();

                    if (catName.isEmpty() || rawAmount.isEmpty() || rawDate.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    long amount = parseAmount(rawAmount);
                    LocalDate date = parseDateFlexible(rawDate);

                    CategoryEntity category = categoryRepository.findByName(catName)
                            .orElseGet(() -> categoryRepository.save(
                                    CategoryEntity.builder().name(catName).type("기타").build()
                            ));

                    boolean exists = consumptionRepository
                            .existsByUser_IdAndDateAndAmountAndCategory_Id(
                                    user.getId(), date, amount, category.getId());

                    if (exists) {
                        skipped++;
                        continue;
                    }

                    ConsumptionEntity entity = ConsumptionEntity.builder()
                            .user(user)
                            .category(category)
                            .date(date)
                            .amount(amount)
                            .build();
                    consumptionRepository.save(entity);
                    inserted++;
                } catch (Exception e) {
                    errorCount++;
                    if (errors.size() < 20) {
                        errors.add("line " + rec.getRecordNumber() + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV 파싱 실패: " + e.getMessage());
        }

        return ConsumptionImportResultDTO.builder()
                .insertedCount(inserted)
                .skippedCount(skipped)
                .errorCount(errorCount)
                .errors(errors)
                .build();
    }

    /* ======================== 내부 유틸 ======================== */

    private long toLongExact(java.math.BigInteger v) {
        try {
            return v.longValueExact();
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "금액이 너무 큽니다");
        }
    }

    private UserEntity currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 사용자 정보를 찾을 수 없습니다."));
    }

    private static String findHeader(Map<String, Integer> headerMap, String... candidates) {
        if (headerMap == null) return null;
        Set<String> keys = new HashSet<>();
        for (String k : headerMap.keySet()) keys.add(normalize(k));
        for (String c : candidates) {
            String nc = normalize(c);
            for (String k : keys) {
                if (k.equals(nc)) {
                    // 원래 키를 다시 찾아 돌려준다
                    for (String original : headerMap.keySet()) {
                        if (normalize(original).equals(k)) return original;
                    }
                }
            }
        }
        return null;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replaceAll("\\s", "");
    }

    private static long parseAmount(String raw) {
        // 숫자/마이너스만 추출 (ex: "12,340원" → 12340)
        String cleaned = raw.replaceAll("[^0-9\\-]", "");
        if (cleaned.isEmpty() || cleaned.equals("-")) return 0L;
        return Long.parseLong(cleaned);
    }

    private static LocalDate parseDateFlexible(String raw) {
        List<String> fmts = List.of(
                "yyyy-MM-dd", "yyyy.MM.dd", "yyyy/MM/dd",
                "yy-MM-dd", "yy.MM.dd", "yy/MM/dd"
        );
        for (String f : fmts) {
            try {
                return LocalDate.parse(raw, DateTimeFormatter.ofPattern(f));
            } catch (DateTimeParseException ignored) {}
        }
        // ISO도 시도
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("지원하지 않는 날짜 형식: " + raw);
        }
    }

    @Transactional
    public void update(Long id, LocalDate date, String category, Long amount) {
        UserEntity user = currentUser();
        ConsumptionEntity entity = consumptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 소비 내역입니다. id=" + id));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 소비 내역에 대한 수정 권한이 없습니다.");
        }

        if (date != null) {
            entity.setDate(date);
        }
        if (category != null && !category.isBlank()) {
            String name = category.trim();
            CategoryEntity cat = categoryRepository.findByName(name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 카테고리입니다. name=" + name));
            entity.setCategory(cat);
        }
        if (amount != null) {
            if (amount < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "금액은 0 이상이어야 합니다.");
            entity.setAmount(amount);
        }
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = currentUser();

        ConsumptionEntity entity = consumptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 소비 내역입니다. id=" + id));

        if (!entity.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 소비 내역에 대한 삭제 권한이 없습니다.");
        }

        consumptionRepository.delete(entity);
    }
}
