package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import com.fiap.feedbackhub.email.NotificacaoCriticaEmailSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class NotificarCriticoFunction {

    @Bean
    public Consumer<SQSEvent> notificarCritico(ObjectMapper objectMapper, NotificacaoCriticaEmailSender emailSender) {
        return event -> {
            for (SQSEvent.SQSMessage mensagem : event.getRecords()) {
                NotificacaoCriticaDTO notificacao;
                try {
                    notificacao = objectMapper.readValue(mensagem.getBody(), NotificacaoCriticaDTO.class);
                } catch (Exception e) {
                    throw new IllegalStateException("Mensagem da fila de críticos em formato inválido", e);
                }
                emailSender.enviar(notificacao);
            }
        };
    }
}
