package com.click.click.budget.entity;


import com.click.click.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="budget")
@Getter@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "budget_month", nullable = false)
    private LocalDate budgetMonth;

    @Column(nullable = false)
    private Long amount;
}
