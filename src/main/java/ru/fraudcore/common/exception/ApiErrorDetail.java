package ru.fraudcore.common.exception;

import lombok.Builder;

@Builder
public record ApiErrorDetail(String field, String message) {
}
