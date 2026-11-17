package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.service.AvaliacaoService;
import com.fiap.feedbackhub.support.AutorizacaoSupport;
import com.fiap.feedbackhub.support.RespostaApiGatewaySupport;
import com.fiap.feedbackhub.support.ValidacaoSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

@Configuration
public class ReceberAvaliacaoFunction {

    private static final String GRUPO_ALUNO = "ALUNO";

    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> receberAvaliacao(
            ObjectMapper objectMapper, Validator validator, AvaliacaoService avaliacaoService) {

        return event -> {
            if (!AutorizacaoSupport.possuiGrupo(event, GRUPO_ALUNO)) {
                return RespostaApiGatewaySupport.resposta(403, "{\"erro\":\"Apenas alunos podem enviar avaliações\"}");
            }

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

            try {
                avaliacaoService.registrar(avaliacaoDTO);
            } catch (NoSuchElementException e) {
                return RespostaApiGatewaySupport.resposta(404, "{\"erro\":\"Aula não encontrada\"}");
            }

            return RespostaApiGatewaySupport.resposta(201, "{\"status\":\"recebido\"}");
        };
    }
}
