package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.AulaDTO;
import com.fiap.feedbackhub.repository.AulaRepository;
import com.fiap.feedbackhub.support.RespostaApiGatewaySupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

@Configuration
public class ListarAulasFunction {

    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> listarAulas(
            ObjectMapper objectMapper, AulaRepository aulaRepository) {

        return event -> {
            List<AulaDTO> aulas = aulaRepository.listarTodas().stream()
                    .map(AulaDTO::de)
                    .toList();

            try {
                return RespostaApiGatewaySupport.resposta(200, objectMapper.writeValueAsString(aulas));
            } catch (Exception e) {
                throw new IllegalStateException("Falha ao serializar lista de aulas", e);
            }
        };
    }
}
