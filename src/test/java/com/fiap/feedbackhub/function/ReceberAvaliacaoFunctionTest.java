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

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withBody("{\"descricao\":\"Aula boa\",\"nota\":8}");

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(201);
        verify(avaliacaoService).registrar(any());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("payloadsInvalidos")
    void deveRetornar400ParaPayloadsInvalidos(String cenario, String body) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withBody(body);

        APIGatewayProxyResponseEvent resposta = receberAvaliacao.apply(event);

        assertThat(resposta.getStatusCode()).isEqualTo(400);
        verify(avaliacaoService, never()).registrar(any());
    }

    private static Stream<Arguments> payloadsInvalidos() {
        return Stream.of(
                Arguments.of("nota fora do intervalo", "{\"descricao\":\"Aula boa\",\"nota\":15}"),
                Arguments.of("descricao em branco", "{\"descricao\":\"\",\"nota\":5}"),
                Arguments.of("json invalido", "não é json")
        );
    }
}
