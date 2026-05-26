package ru.fraudcore.cases.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.fraudcore.cases.dto.FraudCaseDecisionRequest;
import ru.fraudcore.cases.dto.FraudCaseDecisionResponse;
import ru.fraudcore.cases.dto.FraudCaseResponse;
import ru.fraudcore.cases.dto.FraudCaseSummaryResponse;
import ru.fraudcore.cases.entity.FraudCaseStatus;
import ru.fraudcore.cases.service.FraudCaseService;
import ru.fraudcore.common.response.PageResponse;
import ru.fraudcore.transactions.entity.RiskLevel;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class FraudCaseController {

    private final FraudCaseService fraudCaseService;

    @GetMapping
    public PageResponse<FraudCaseSummaryResponse> findAll(
            @RequestParam(required = false) FraudCaseStatus status,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(required = false) Boolean assignedToMe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return fraudCaseService.findAll(status, riskLevel, assignedToMe, dateFrom, dateTo, sortBy, sortDirection, page, size);
    }

    @GetMapping("/{id}")
    public FraudCaseResponse getById(@PathVariable Long id) {
        return fraudCaseService.getById(id);
    }

    @PostMapping("/{id}/assign-to-me")
    public FraudCaseResponse assignToMe(@PathVariable Long id) {
        return fraudCaseService.assignToMe(id);
    }

    @PostMapping("/{id}/decision")
    public FraudCaseDecisionResponse decision(@PathVariable Long id, @Valid @RequestBody FraudCaseDecisionRequest request) {
        return fraudCaseService.makeDecision(id, request);
    }
}
