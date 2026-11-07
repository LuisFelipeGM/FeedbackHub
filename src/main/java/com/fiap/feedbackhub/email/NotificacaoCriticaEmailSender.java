package com.fiap.feedbackhub.email;

import com.fiap.feedbackhub.domain.dto.NotificacaoCriticaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Component
public class NotificacaoCriticaEmailSender {

    private final SesClient sesClient;
    private final String remetente;
    private final String destinatarioAdmin;

    public NotificacaoCriticaEmailSender(
            SesClient sesClient,
            @Value("${aws.ses.remetente}") String remetente,
            @Value("${aws.ses.destinatario-admin}") String destinatarioAdmin
    ) {
        this.sesClient = sesClient;
        this.remetente = remetente;
        this.destinatarioAdmin = destinatarioAdmin;
    }

    public void enviar(NotificacaoCriticaDTO notificacao) {
        sesClient.sendEmail(SendEmailRequest.builder()
                .source(remetente)
                .destination(Destination.builder().toAddresses(destinatarioAdmin).build())
                .message(Message.builder()
                        .subject(Content.builder().data("Avaliação crítica recebida").build())
                        .body(Body.builder()
                                .text(Content.builder().data(corpoTexto(notificacao)).build())
                                .html(Content.builder().data(corpoHtml(notificacao)).build())
                                .build())
                        .build())
                .build());
    }

    private String corpoTexto(NotificacaoCriticaDTO notificacao) {
        return "Descrição: " + notificacao.descricao() + "\n"
                + "Urgência: " + notificacao.urgencia() + "\n"
                + "Data de envio: " + notificacao.dataEnvio();
    }

    private String corpoHtml(NotificacaoCriticaDTO notificacao) {
        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #c0392b;">Avaliação crítica recebida</h2>
                    <table style="border-collapse: collapse;">
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Descrição</td>
                        <td style="padding: 4px 0;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Urgência</td>
                        <td style="padding: 4px 0; color: #c0392b; font-weight: bold;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Data de envio</td>
                        <td style="padding: 4px 0;">%s</td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(
                escaparHtml(notificacao.descricao()),
                escaparHtml(notificacao.urgencia().name()),
                escaparHtml(notificacao.dataEnvio()));
    }

    private String escaparHtml(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
