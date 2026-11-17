package com.fiap.feedbackhub.support;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;

import java.util.Map;

public final class AutorizacaoSupport {

    private AutorizacaoSupport() {
    }

    public static boolean possuiGrupo(APIGatewayProxyRequestEvent event, String grupo) {
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = event.getRequestContext();
        if (requestContext == null) {
            return false;
        }

        Map<String, Object> authorizer = requestContext.getAuthorizer();
        if (authorizer == null) {
            return false;
        }

        Object claims = authorizer.get("claims");
        if (!(claims instanceof Map<?, ?> claimsMap)) {
            return false;
        }

        Object grupos = claimsMap.get("cognito:groups");
        return grupos != null && grupos.toString().contains(grupo);
    }
}
