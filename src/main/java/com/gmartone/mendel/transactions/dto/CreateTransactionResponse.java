package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create transaction response")
public record CreateTransactionResponse(
        @Schema(description = "Status of the creation, (ok)")
        String status
) {}
