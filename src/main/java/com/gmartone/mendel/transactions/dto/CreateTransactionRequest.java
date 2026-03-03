package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Schema(description = "Transaction creation request payload")
public record CreateTransactionRequest(

        @Schema(
                description = "Amount of the transaction – must be a positive number",
                example = "4000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "amount must be a positive number")
        double amount,

        @Schema(
                description = "Transaction type (used for grouping)",
                example = "shopping",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "type must not be blank")
        String type,

        @Schema(
                description = "Optional parent transaction id",
                example = "1",
                nullable = true
        )
        Long parent_id
) { }
