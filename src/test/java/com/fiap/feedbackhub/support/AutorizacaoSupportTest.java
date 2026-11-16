package com.fiap.feedbackhub.support;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AutorizacaoSupportTest {

    @Test
    void deveRetornarTrueQuandoGrupoEstaPresente() {
        APIGatewayProxyRequestEvent event = eventoComClaims(Map.of("cognito:groups", "[ALUNO]"));

        assertThat(AutorizacaoSupport.possuiGrupo(event, "ALUNO")).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoGrupoNaoEstaPresente() {
        APIGatewayProxyRequestEvent event = eventoComClaims(Map.of("cognito:groups", "[ADMIN]"));

        assertThat(AutorizacaoSupport.possuiGrupo(event, "ALUNO")).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoClaimDeGruposNaoExiste() {
        APIGatewayProxyRequestEvent event = eventoComClaims(Map.of("sub", "123"));

        assertThat(AutorizacaoSupport.possuiGrupo(event, "ALUNO")).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoNaoHaAuthorizer() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());

        assertThat(AutorizacaoSupport.possuiGrupo(event, "ALUNO")).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoNaoHaRequestContext() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();

        assertThat(AutorizacaoSupport.possuiGrupo(event, "ALUNO")).isFalse();
    }

    private APIGatewayProxyRequestEvent eventoComClaims(Map<String, Object> claims) {
        Map<String, Object> authorizer = Map.of("claims", claims);
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        requestContext.setAuthorizer(authorizer);

        return new APIGatewayProxyRequestEvent()
                .withRequestContext(requestContext);
    }
}
