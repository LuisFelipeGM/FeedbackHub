package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.entity.Aula;
import com.fiap.feedbackhub.repository.AulaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarAulasFunctionTest {

    @Mock
    private AulaRepository aulaRepository;

    private Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> listarAulas;

    @BeforeEach
    void setup() {
        listarAulas = new ListarAulasFunction().listarAulas(new ObjectMapper(), aulaRepository);
    }

    @Test
    void deveRetornar200ComAListaDeAulas() {
        Aula aula = new Aula();
        aula.setId("aula-01");
        aula.setTitulo("Introdução à Arquitetura Serverless");
        when(aulaRepository.listarTodas()).thenReturn(List.of(aula));

        APIGatewayProxyResponseEvent resposta = listarAulas.apply(new APIGatewayProxyRequestEvent());

        assertThat(resposta.getStatusCode()).isEqualTo(200);
        assertThat(resposta.getBody())
                .contains("aula-01")
                .contains("Introdução à Arquitetura Serverless");
    }

    @Test
    void deveRetornar200ComListaVaziaQuandoNaoHaAulas() {
        when(aulaRepository.listarTodas()).thenReturn(List.of());

        APIGatewayProxyResponseEvent resposta = listarAulas.apply(new APIGatewayProxyRequestEvent());

        assertThat(resposta.getStatusCode()).isEqualTo(200);
        assertThat(resposta.getBody()).isEqualTo("[]");
    }
}
