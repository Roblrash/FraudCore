package ru.fraudcore.transactions.mapper;

import org.mapstruct.Mapper;
import ru.fraudcore.transactions.dto.CreateTransactionRequest;
import ru.fraudcore.transactions.dto.TransactionResponse;
import ru.fraudcore.transactions.entity.Transaction;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    Transaction toEntity(CreateTransactionRequest request);

    TransactionResponse toResponse(Transaction transaction);
}
