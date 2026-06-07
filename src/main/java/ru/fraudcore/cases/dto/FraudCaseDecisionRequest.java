package ru.fraudcore.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.fraudcore.cases.entity.FraudCaseDecision;

public record FraudCaseDecisionRequest(
        @NotNull FraudCaseDecision decision,
        @NotBlank @Size(max = 4000) String comment
) {
}
