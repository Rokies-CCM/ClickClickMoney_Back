package com.click.click.consumption.service;

import com.click.click.budget.entity.BudgetEntity;
import com.click.click.budget.repository.BudgetRepository;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ConsumptionRepository consumptionRepository;
    private final BudgetRepository budgetRepository;

    /* ======================== 카테고리 자동 분류 (신규) ======================== */

    /**
     * 가맹점명으로 카테고리 자동 추론
     * CSV merchant 필드나 기타 정보로 카테고리 자동 분류
     */
    private String autoCategorize(String merchant, String originalCategory) {
        // 이미 "기타"가 아닌 경우 그대로 사용
        if (originalCategory != null && !originalCategory.equals("기타")) {
            return originalCategory;
        }

        if (merchant == null) {
            return "기타";
        }

        String m = merchant.toLowerCase();

        // 카페/음료
        if (containsAny(m, "스타벅스", "커피", "카페", "cafe", "coffee", "이디야", "투썸", "메가커피", "빽다방", "할리스", "탐앤탐스")) {
            return "카페/음료";
        }

        // 편의점
        if (containsAny(m, "gs25", "cu", "세븐일레븐", "7-eleven", "편의점", "미니스톱", "이마트24")) {
            return "편의점";
        }

        // 패스트푸드
        if (containsAny(m, "맥도날드", "버거킹", "롯데리아", "kfc", "맘스터치", "노브랜드버거", "파파이스", "서브웨이")) {
            return "패스트푸드";
        }

        // 배달음식
        if (containsAny(m, "배달", "요기요", "배민", "쿠팡이츠", "배달의민족")) {
            return "배달음식";
        }

        // 외식
        if (containsAny(m, "식당", "음식점", "한식", "중식", "일식", "양식", "치킨", "피자", "찜닭", "김밥", "분식")) {
            return "외식";
        }

        // 교통
        if (containsAny(m, "버스", "지하철", "택시", "카카오t", "우버", "타다", "쏘카", "그린카")) {
            return "교통";
        }

        // 마트/장보기
        if (containsAny(m, "이마트", "홈플러스", "롯데마트", "하나로마트", "마트", "costco", "코스트코")) {
            return "마트/장보기";
        }

        // 생활용품
        if (containsAny(m, "올리브영", "다이소", "왓슨스", "lalavla", "아리따움")) {
            return "생활용품";
        }

        // 문화/여가
        if (containsAny(m, "cgv", "롯데시네마", "메가박스", "영화", "pc방", "노래방", "찜질방")) {
            return "문화/여가";
        }

        // 의료/건강
        if (containsAny(m, "병원", "약국", "의원", "한의원", "클리닉", "치과")) {
            return "의료/건강";
        }

        // 온라인쇼핑
        if (containsAny(m, "쿠팡", "11번가", "지마켓", "옥션", "네이버페이", "카카오페이", "ssg", "쓱")) {
            return "온라인쇼핑";
        }

        return "기타";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /* ======================== 단건/배열 저장(기존) ======================== */
    @Transactional
    public void record(ConsumptionDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 사용자 정보를 찾을 수 없습니다."));

        for (ConsumptionDTO.Item item : dto.getItems()) {
            // 카테고리 자동 분류 (merchant가 있으면 사용)
            String categoryName = autoCategorize(item.getMerchant(), item.getCategory());

            CategoryEntity category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(
                            CategoryEntity.builder()
                                    .name(categoryName)
                                    .type("기타")
                                    .build()
                    ));

            long amount = toLongExact(item.getAmount());

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

        YearMonth ym = YearMonth.of(from.getYear(), from.getMonth());
        LocalDate firstDay = ym.atDay(1);
        long targetBudget = budgetRepository
                .findAllByUserAndBudgetMonth(user, firstDay)
                .stream()
                .map(BudgetEntity::getAmount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        return MonthlyDashboardDTO.builder()
                .from(from)
                .to(to)
                .yearMonth(String.format("%d-%02d", from.getYear(), from.getMonthValue()))
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .byCategory(breakdown)
                .targetBudget(targetBudget)
                .build();
    }

    /* ======================== CSV 업로드 (카테고리 자동 분류 추가) ======================== */
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
            String hMerchant = findHeader(headerMap, "merchant", "가맹점", "가맹점명", "상호");

            if (hDate == null || hAmount == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "CSV 헤더를 인식할 수 없습니다. (필수: date/날짜, amount/금액)");
            }

            for (CSVRecord rec : parser) {
                try {
                    String catName = (hCategory != null) ? rec.get(hCategory).trim() : "기타";
                    String merchant = (hMerchant != null) ? rec.get(hMerchant).trim() : null;
                    String rawAmount = rec.get(hAmount).trim();
                    String rawDate = rec.get(hDate).trim();

                    if (rawAmount.isEmpty() || rawDate.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // 카테고리 자동 분류 (merchant 우선)
                    String finalCategory = autoCategorize(merchant, catName);

                    long amount = parseAmount(rawAmount);
                    LocalDate date = parseDateFlexible(rawDate);

                    CategoryEntity category = categoryRepository.findByName(finalCategory)
                            .orElseGet(() -> categoryRepository.save(
                                    CategoryEntity.builder().name(finalCategory).type("기타").build()
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