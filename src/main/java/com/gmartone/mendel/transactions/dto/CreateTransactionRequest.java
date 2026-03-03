package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

@Schema(description = "Transaction creation request payload")
public record CreateTransactionRequest(

        @Schema(
                description = "Amount of the transaction – must be a positive number",
                example = "4000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "amount must be a positive number")
        @Digits(integer = 18, fraction = 2, message = "amount must have at most 18 integer digits and 2 decimal places")
        BigDecimal amount,

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
