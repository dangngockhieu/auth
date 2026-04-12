package com.example.auth.util.format_response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RestResponse<T>(
        boolean success,
        String message,
        T data,
        Map<String, String> errors,
        OffsetDateTime timestamp) {

    public static <T> RestResponse<T> success(String message, T data) {
        return new RestResponse<>(true, message, data, null, OffsetDateTime.now(ZoneOffset.UTC));
    }

    public static RestResponse<Void> error(String message, Map<String, String> errors) {
        return new RestResponse<>(false, message, null, errors, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
