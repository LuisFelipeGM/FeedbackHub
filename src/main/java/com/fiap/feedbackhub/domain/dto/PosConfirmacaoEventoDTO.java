package com.fiap.feedbackhub.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PosConfirmacaoEventoDTO(
        String version,
        String triggerSource,
        String region,
        String userPoolId,
        String userName,
        Object callerContext,
        RequestDTO request,
        Map<String, Object> response
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RequestDTO(Map<String, String> userAttributes) {
    }
}
