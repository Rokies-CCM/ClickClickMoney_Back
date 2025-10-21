package com.click.click.consumption.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity @Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uk_categories_name", columnNames = "name"))
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String type;                     // 예: 필수 지출, 선택 지출, 기타
}
