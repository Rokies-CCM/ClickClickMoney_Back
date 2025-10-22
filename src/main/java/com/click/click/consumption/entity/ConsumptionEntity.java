package com.click.click.consumption.entity;

import com.click.click.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "consumption")
public class ConsumptionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Long amount;

    @OneToMany(mappedBy = "consumption", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MemoEntity> memos = new ArrayList<>();
}