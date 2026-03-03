package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Transactions", description = "Endpoints to create and retrieve transactions")
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionsService transactionService;

    public TransactionsController(TransactionsService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Create a new transaction",
            description = "Creates a transaction with the specified id. If parent_id is provided, it must exist."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateTransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body, id already in use, or parent not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PutMapping("/{id:\\d+}")
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @Parameter(description = "Transaction id", example = "4", required = true)
            @PathVariable long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Transaction creation payload",
                    required = true
            )
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        Transaction transaction = new Transaction(
                id,
                request.amount(),
                request.type(),
                request.parent_id()
        );

        transactionService.create(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateTransactionResponse("ok"));
    }

    @Operation(
            summary = "Find transactions by type",
            description = "Returns a list of transaction ids that match the provided type"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully (empty list if none match)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(type = "integer", example = "1")),
                            examples = @ExampleObject(value = "[1, 2]")
                    )
            )
    })
    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> findByType(
            @Parameter(description = "Transaction type", example = "shopping", required = true)
            @PathVariable String type
    ) {
        return ResponseEntity.ok(transactionService.findByType(type));
    }

    @Operation(
            summary = "Calculate aggregated transaction sum",
            description = "Returns the sum of a transaction's amount plus all its transitive children"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sum calculated successfully"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/sum/{id:\\d+}")
    public ResponseEntity<SumResponse> sum(
            @Parameter(description = "Transaction id", example = "1", required = true)
            @PathVariable long id
    ) {
        double total = transactionService.sum(id);
        return ResponseEntity.ok(new SumResponse(total));
    }
}
