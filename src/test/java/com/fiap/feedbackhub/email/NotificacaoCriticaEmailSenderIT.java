package com.fiap.feedbackhub.email;

import com.fiap.feedbackhub.config.AbstractIT;
import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class NotificacaoCriticaEmailSenderIT extends AbstractIT {

    @Autowired
    private NotificacaoCriticaEmailSender emailSender;

    @Test
    void deveEnviarEmailSemLancarExcecao() {
        NotificacaoCriticaDTO notificacao = new NotificacaoCriticaDTO(
                "Aula muito ruim, professor não apareceu", Urgencia.ALTA, "2026-11-06T10:15:30");

        assertThatCode(() -> emailSender.enviar(notificacao)).doesNotThrowAnyException();
    }
}
