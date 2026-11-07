package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import com.fiap.feedbackhub.email.NotificacaoCriticaEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class NotificarCriticoFunctionTest {

    @Mock
    private NotificacaoCriticaEmailSender emailSender;

    private Consumer<SQSEvent> notificarCritico;

    @BeforeEach
    void setup() {
        notificarCritico = new NotificarCriticoFunction().notificarCritico(new ObjectMapper(), emailSender);
    }

    @Test
    void deveEnviarUmEmailPorMensagemDaFila() {
        SQSEvent event = eventoComCorpos(
                "{\"descricao\":\"Aula ruim\",\"urgencia\":\"ALTA\",\"dataEnvio\":\"2026-11-06T10:00:00\"}");

        notificarCritico.accept(event);

        ArgumentCaptor<NotificacaoCriticaDTO> captor = ArgumentCaptor.forClass(NotificacaoCriticaDTO.class);
        verify(emailSender).enviar(captor.capture());
        assertThat(captor.getValue().descricao()).isEqualTo("Aula ruim");
    }

    @Test
    void deveLancarExcecaoQuandoMensagemEmFormatoInvalido() {
        SQSEvent event = eventoComCorpos("não é json");

        assertThatThrownBy(() -> notificarCritico.accept(event))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(emailSender);
    }

    private SQSEvent eventoComCorpos(String... corpos) {
        SQSEvent event = new SQSEvent();
        event.setRecords(Stream.of(corpos).map(corpo -> {
            SQSEvent.SQSMessage mensagem = new SQSEvent.SQSMessage();
            mensagem.setBody(corpo);
            return mensagem;
        }).toList());
        return event;
    }
}
