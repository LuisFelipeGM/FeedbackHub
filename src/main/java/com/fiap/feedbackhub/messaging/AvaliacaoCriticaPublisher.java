package com.fiap.feedbackhub.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


@Component
public class AvaliacaoCriticaPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String filaUrl;

    public AvaliacaoCriticaPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.fila-notificacoes-criticas}") String filaNome
    ) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.filaUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                        .queueName(filaNome)
                        .build())
                .queueUrl();
    }

    public void publicar(Avaliacao avaliacao) {
        NotificacaoCriticaDTO notificacao = NotificacaoCriticaDTO.de(avaliacao);

        String corpo;
        try {
            corpo = objectMapper.writeValueAsString(notificacao);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar notificação crítica", e);
        }

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(filaUrl)
                .messageBody(corpo)
                .build());
    }
}
