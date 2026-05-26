package ru.fraudcore.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.fraudcore.cases.entity.FraudCaseDecision;

public record FraudCaseDecisionRequest(
        @NotNull FraudCaseDecision decision,
        @NotBlank String comment
) {
}
