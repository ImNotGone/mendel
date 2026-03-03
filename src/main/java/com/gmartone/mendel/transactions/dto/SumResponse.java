package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Aggregated sum response")
public record SumResponse(

        @Schema(
                description = "Sum of the transaction amount including all its transitive children",
                example = "20000.00"
        )
        BigDecimal sum
) { }
