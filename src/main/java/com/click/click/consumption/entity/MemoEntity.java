package com.click.click.consumption.entity;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="memo")
public class MemoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="consumption_id",nullable = false)
    private ConsumptionEntity consumption;


    @Column(name = "memo", nullable = false, length = 1000)
    private String value;
}
