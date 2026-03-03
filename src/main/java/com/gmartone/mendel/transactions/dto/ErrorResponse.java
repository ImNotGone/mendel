package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error response payload")
public record ErrorResponse(

        @Schema(
                description = "Error message describing what went wrong",
                example = "Transaction id already exists"
        )
        String message
) {}