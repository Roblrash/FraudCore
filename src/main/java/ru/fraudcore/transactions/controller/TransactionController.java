package ru.fraudcore.transactions.controller;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.fraudcore.common.response.PageResponse;
import ru.fraudcore.transactions.dto.CreateTransactionRequest;
import ru.fraudcore.transactions.dto.RiskExplanationResponse;
import ru.fraudcore.transactions.dto.TransactionAcceptedResponse;
import ru.fraudcore.transactions.dto.TransactionResponse;
import ru.fraudcore.transactions.entity.RiskLevel;
import ru.fraudcore.transactions.entity.TransactionStatus;
import ru.fraudcore.transactions.entity.TransactionType;
import ru.fraudcore.transactions.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public TransactionAcceptedResponse create(@Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(request);
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping
    public PageResponse<TransactionResponse> findAll(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String externalId,
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(
                    description = "Поле сортировки. riskLevel сортируется по числовому riskScore",
                    example = "createdAt",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            allowableValues = {"createdAt", "amount", "riskScore", "riskLevel"}
                    )
            )
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(
                    description = "Направление сортировки",
                    example = "desc",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(
                            allowableValues = {"asc", "desc"}
                    )
            )
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return transactionService.findAll(
                clientId,
                externalId,
                status,
                type,
                riskLevel,
                dateFrom,
                dateTo,
                minAmount,
                maxAmount,
                sortBy,
                sortDirection,
                page,
                size
        );
    }

    @GetMapping("/{id}/risk-explanation")
    public RiskExplanationResponse getRiskExplanation(@PathVariable Long id) {
        return transactionService.getRiskExplanation(id);
    }
}
