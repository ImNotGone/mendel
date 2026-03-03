package com.gmartone.mendel.transactions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema( description = "A transactions snapshot")
public record Transaction(
        @Schema(description = "The transactions id")
        long id,

        @Schema(description = "The amount spent in the transaction")
        double amount,

        @Schema(description = "The transactions type")
        String type,

        @Schema(description = "The transactions parent transaction id")
        Long parent_id
) { }
