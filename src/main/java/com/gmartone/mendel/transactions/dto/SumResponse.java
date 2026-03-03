package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregated sum response")
public record SumResponse(

        @Schema(
                description = "Sum of the transaction amount including all its children recursively",
                example = "20000.0"
        )
        double sum
) { }