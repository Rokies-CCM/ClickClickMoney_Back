package com.click.click.util;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Spring Data Page 직렬화 경고(PageImpl as-is)를 피하기 위한 안정적 DTO.
 * 프론트에 고정된 JSON 스키마를 제공한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponse<T> from(Page<T> p) {
        return PageResponse.<T>builder()
                .content(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .first(p.isFirst())
                .last(p.isLast())
                .empty(p.isEmpty())
                .build();
    }
}
