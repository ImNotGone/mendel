package com.gmartone.mendel.transactions.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Transaction snapshot representation")
public record Transaction(

        @Schema(
                description = "Unique transaction identifier",
                example = "4"
        )
        long id,

        @Schema(
                description = "Amount of the transaction",
                example = "4000"
        )
        double amount,

        @Schema(
                description = "Transaction type",
                example = "insurance"
        )
        String type,

        @Schema(
                description = "Optional parent transaction id",
                example = "1",
                nullable = true
        )
        Long parent_id
) { }