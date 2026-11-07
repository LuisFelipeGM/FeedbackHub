package com.fiap.feedbackhub.email;

import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificacaoCriticaEmailSenderTest {

    @Mock
    private SesClient sesClient;

    private NotificacaoCriticaEmailSender emailSender;

    @BeforeEach
    void setup() {
        emailSender = new NotificacaoCriticaEmailSender(sesClient, "admin@feedbackhub.local", "coordenacao@feedbackhub.local");
    }

    @Test
    void deveMontarEEnviarEmailComOsDadosDaNotificacao() {
        NotificacaoCriticaDTO notificacao = new NotificacaoCriticaDTO(
                "Aula muito ruim, professor não apareceu", Urgencia.ALTA, "2026-11-06T10:15:30");

        emailSender.enviar(notificacao);

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        SendEmailRequest requisicao = captor.getValue();
        assertThat(requisicao.source()).isEqualTo("admin@feedbackhub.local");
        assertThat(requisicao.destination().toAddresses()).containsExactly("coordenacao@feedbackhub.local");
        assertThat(requisicao.message().body().text().data())
                .contains("Aula muito ruim, professor não apareceu")
                .contains("ALTA")
                .contains("2026-11-06T10:15:30");
        assertThat(requisicao.message().body().html().data())
                .contains("Aula muito ruim, professor não apareceu")
                .contains("ALTA")
                .contains("2026-11-06T10:15:30")
                .contains("<html>");
    }

    @Test
    void deveEscaparCaracteresEspeciaisNoHtml() {
        NotificacaoCriticaDTO notificacao = new NotificacaoCriticaDTO(
                "Aula <ruim> & \"confusa\"", Urgencia.ALTA, "2026-11-06T10:15:30");

        emailSender.enviar(notificacao);

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
        verify(sesClient).sendEmail(captor.capture());

        assertThat(captor.getValue().message().body().html().data())
                .contains("Aula &lt;ruim&gt; &amp; &quot;confusa&quot;")
                .doesNotContain("<ruim>");
    }
}
