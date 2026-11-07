package com.fiap.feedbackhub.support;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.Map;

public final class RespostaApiGatewaySupport {

    private RespostaApiGatewaySupport() {
    }

    public static APIGatewayProxyResponseEvent resposta(int status, String corpo) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(corpo);
    }
}
