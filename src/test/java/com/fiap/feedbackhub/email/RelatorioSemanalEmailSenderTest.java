package com.fiap.feedbackhub.email;

import com.fiap.feedbackhub.domain.dto.AvaliacaoResumoDTO;
import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RelatorioSemanalEmailSenderTest {

    @Mock
    private SesClient sesClient;

    private RelatorioSemanalEmailSender emailSender;

    @BeforeEach
    void setup() {
        emailSender = new RelatorioSemanalEmailSender(sesClient, "admin@feedbackhub.local", "coordenacao@feedbackhub.local");
    }

    @Test
    void deveEnviarEmailComAnexoCsv() throws Exception {
        RelatorioSemanalDTO relatorio = new RelatorioSemanalDTO(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 7),
                7.5,
                Map.of(),
                Map.of(),
                List.of(new AvaliacaoResumoDTO("Aula boa", Urgencia.BAIXA, "2026-11-01T10:00:00")));

        emailSender.enviar(relatorio, "relatorios/2026-11-01_2026-11-07.csv", "conteudo,do,csv");

        ArgumentCaptor<SendRawEmailRequest> captor = ArgumentCaptor.forClass(SendRawEmailRequest.class);
        verify(sesClient).sendRawEmail(captor.capture());

        byte[] bytesEnviados = captor.getValue().rawMessage().data().asByteArray();
        MimeMessage mensagem = new MimeMessage(
                Session.getDefaultInstance(new Properties()),
                new ByteArrayInputStream(bytesEnviados));

        assertThat(mensagem.getSubject()).isEqualTo("Relatório semanal de avaliações");
        assertThat(mensagem.getFrom()[0].toString()).contains("admin@feedbackhub.local");

        MimeMultipart conteudoMisto = (MimeMultipart) mensagem.getContent();
        assertThat(conteudoMisto.getCount()).isEqualTo(2);

        MimeBodyPart partAnexo = (MimeBodyPart) conteudoMisto.getBodyPart(1);
        assertThat(partAnexo.getFileName()).isEqualTo("relatorio-semanal-2026-11-01_2026-11-07.csv");
        assertThat(partAnexo.getContentType()).contains("text/csv");

        String conteudoAnexo = new String(partAnexo.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertThat(conteudoAnexo).isEqualTo("conteudo,do,csv");
    }
}
