package ru.fraudcore.cases.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.fraudcore.cases.dto.FraudCaseSummaryResponse;
import ru.fraudcore.cases.entity.FraudCase;

@Mapper(componentModel = "spring")
public interface FraudCaseMapper {

    @Mapping(target = "transactionId", source = "transaction.id")
    @Mapping(target = "assignedAnalystId", source = "assignedAnalyst.id")
    FraudCaseSummaryResponse toSummary(FraudCase fraudCase);
}
