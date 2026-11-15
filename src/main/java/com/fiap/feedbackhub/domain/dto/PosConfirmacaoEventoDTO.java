package com.fiap.feedbackhub.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosConfirmacaoEventoDTO(
        String userPoolId,
        String userName,
        RequestDTO request,
        Map<String, Object> response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RequestDTO(Map<String, String> userAttributes) {
    }
}
