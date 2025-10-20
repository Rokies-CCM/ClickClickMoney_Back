// src/main/java/com/click/click/dto/ConsumptionSummaryDTO.java
package com.click.click.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionSummaryDTO {
    private String categoryName; // c.name
    private Long totalAmount;    // sum(e.amount)
}
