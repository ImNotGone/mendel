package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction creation request payload")
public record CreateTransactionRequest(

        @Schema(
                description = "Amount of the transaction",
                example = "4000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        double amount,

        @Schema(
                description = "Transaction type (used for grouping)",
                example = "shopping",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String type,

        @Schema(
                description = "Optional parent transaction id",
                example = "1",
                nullable = true
        )
        Long parent_id
) { }