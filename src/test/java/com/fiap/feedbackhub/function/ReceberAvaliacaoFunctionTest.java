package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.service.AvaliacaoService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReceberAvaliacaoFunctionTest {

    @Mock
    private AvaliacaoService avaliacaoService;

    private Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> receberAvaliacao;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        receberAvaliacao = new ReceberAvaliacaoFunction()
                .receberAvaliacao(objectMapper, validator, avaliacaoService);
    }

    @Test
    void deveRetornar201QuandoPayloadValido() {
        APIGatewayProxyRequestEvent event = eventoComGrupo("ALUNO", "{\"descricao\":\"Aula boa\",\"nota\":8,\"aulaId\":\"aula-01\"}");

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(201);
        verify(avaliacaoService).registrar(any());
    }

    @Test
    void deveRetornar404QuandoAulaNaoExiste() {
        doThrow(new NoSuchElementException("Aula não encontrada")).when(avaliacaoService).registrar(any());

        APIGatewayProxyRequestEvent event = eventoComGrupo("ALUNO", "{\"descricao\":\"Aula boa\",\"nota\":8,\"aulaId\":\"aula-inexistente\"}");

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(404);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("payloadsInvalidos")
    void deveRetornar400ParaPayloadsInvalidos(String cenario, String body) {
        APIGatewayProxyRequestEvent event = eventoComGrupo("ALUNO", body);

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(400);
        verify(avaliacaoService, never()).registrar(any());
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoEhAluno() {
        APIGatewayProxyRequestEvent event = eventoComGrupo("ADMIN", "{\"descricao\":\"Aula boa\",\"nota\":8}");

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(403);
        verify(avaliacaoService, never()).registrar(any());
    }

    @Test
    void deveRetornar403QuandoNaoHaGrupoNoToken() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withBody("{\"descricao\":\"Aula boa\",\"nota\":8}")
                .withRequestContext(new APIGatewayProxyRequestEvent.ProxyRequestContext());

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(403);
        verify(avaliacaoService, never()).registrar(any());
    }

    private static Stream<Arguments> payloadsInvalidos() {
        return Stream.of(
                Arguments.of("nota fora do intervalo", "{\"descricao\":\"Aula boa\",\"nota\":15,\"aulaId\":\"aula-01\"}"),
                Arguments.of("descricao em branco", "{\"descricao\":\"\",\"nota\":5,\"aulaId\":\"aula-01\"}"),
                Arguments.of("aulaId em branco", "{\"descricao\":\"Aula boa\",\"nota\":5,\"aulaId\":\"\"}"),
                Arguments.of("json invalido", "não é json")
        );
    }

    private APIGatewayProxyRequestEvent eventoComGrupo(String grupo, String body) {
        Map<String, Object> claims = Map.of("cognito:groups", "[" + grupo + "]");
        Map<String, Object> authorizer = Map.of("claims", claims);
        APIGatewayProxyRequestEvent.ProxyRequestContext requestContext = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        requestContext.setAuthorizer(authorizer);

        return new APIGatewayProxyRequestEvent()
                .withBody(body)
                .withRequestContext(requestContext);
    }
}
