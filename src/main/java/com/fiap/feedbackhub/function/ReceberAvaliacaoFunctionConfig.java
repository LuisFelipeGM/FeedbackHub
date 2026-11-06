package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.service.AvaliacaoService;
import com.fiap.feedbackhub.support.RespostaApiGatewaySupport;
import com.fiap.feedbackhub.support.ValidacaoSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.function.Function;

/**
 * Adapter da Lambda ReceberAvaliacao: traduz o evento do API Gateway pra
 * chamada do AvaliacaoService. O roteamento POST /avaliacao -> esta Lambda é
 * feito na infraestrutura (API Gateway), não aqui — essa função não tem rota,
 * só faz essa única coisa.
 */
@Configuration
public class ReceberAvaliacaoFunctionConfig {

    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> receberAvaliacao(
            ObjectMapper objectMapper, Validator validator, AvaliacaoService avaliacaoService) {

        return event -> {
            AvaliacaoDTO avaliacaoDTO;
            try {
                avaliacaoDTO = objectMapper.readValue(event.getBody(), AvaliacaoDTO.class);
            } catch (Exception e) {
                return RespostaApiGatewaySupport.resposta(400, "{\"erro\":\"JSON inválido\"}");
            }

            Set<ConstraintViolation<AvaliacaoDTO>> violacoes = validator.validate(avaliacaoDTO);
            if (!violacoes.isEmpty()) {
                String erro = "{\"erro\":\"" + ValidacaoSupport.mensagemErro(violacoes) + "\"}";
                return RespostaApiGatewaySupport.resposta(400, erro);
            }

            avaliacaoService.registrar(avaliacaoDTO);
            return RespostaApiGatewaySupport.resposta(201, "{\"status\":\"recebido\"}");
        };
    }
}
