package ru.fraudcore.transactions.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.fraudcore.transactions.dto.CreateTransactionRequest;
import ru.fraudcore.transactions.dto.TransactionResponse;
import ru.fraudcore.transactions.entity.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "riskScore", ignore = true)
    @Mapping(target = "riskLevel", ignore = true)
    @Mapping(target = "processedAt", ignore = true)
    Transaction toEntity(CreateTransactionRequest request);

    TransactionResponse toResponse(Transaction transaction);
}
