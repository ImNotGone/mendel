package com.gmartone.mendel.transactions;

import com.gmartone.mendel.transactions.dto.CreateTransactionRequest;
import com.gmartone.mendel.transactions.dto.CreateTransactionResponse;
import com.gmartone.mendel.transactions.dto.Transaction;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Transactions", description = "Endpoints create and retrieve transactions")
@RequestMapping("/transactions")
public class TransactionsController {

    private final TransactionsService transactionService;

    public TransactionsController(TransactionsService transactionService) {
        this.transactionService = transactionService;
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<CreateTransactionResponse> createTransaction(
            @PathVariable long id,
            @RequestBody CreateTransactionRequest request
    ) {

        Transaction transaction = new Transaction(
                id,
                request.amount(),
                request.type(),
                request.parent_id()
        );

        try {
            Transaction created = transactionService.create(transaction);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new CreateTransactionResponse("ok"));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/types/{type}")
    public ResponseEntity<List<Long>> findByType(@PathVariable String type) {
        return ResponseEntity.ok(transactionService.findByType(type));
    }

    @GetMapping("/sum/{id:\\d+}")
    public ResponseEntity<Map<String, Double>> sum(@PathVariable long id) {

        double total = transactionService.sum(id);

        return ResponseEntity.ok(Map.of("sum", total));
    }
}