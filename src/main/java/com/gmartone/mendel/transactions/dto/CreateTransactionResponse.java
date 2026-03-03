package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction creation result")
public record CreateTransactionResponse(

        @Schema(
                description = "Creation status",
                example = "ok"
        )
        String status
) {}
