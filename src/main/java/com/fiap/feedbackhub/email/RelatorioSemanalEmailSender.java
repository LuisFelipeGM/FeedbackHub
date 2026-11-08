package com.fiap.feedbackhub.email;

import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import jakarta.activation.DataHandler;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

@Component
public class RelatorioSemanalEmailSender {

    private final SesClient sesClient;
    private final String remetente;
    private final String destinatarioAdmin;

    public RelatorioSemanalEmailSender(
            SesClient sesClient,
            @Value("${aws.ses.remetente}") String remetente,
            @Value("${aws.ses.destinatario-admin}") String destinatarioAdmin
    ) {
        this.sesClient = sesClient;
        this.remetente = remetente;
        this.destinatarioAdmin = destinatarioAdmin;
    }

    public void enviar(RelatorioSemanalDTO relatorio, String chaveS3, String conteudoCsv) {
        byte[] mensagemBruta = montarMensagem(relatorio, chaveS3, conteudoCsv);

        sesClient.sendRawEmail(SendRawEmailRequest.builder()
                .rawMessage(RawMessage.builder().data(SdkBytes.fromByteArray(mensagemBruta)).build())
                .build());
    }

    private byte[] montarMensagem(RelatorioSemanalDTO relatorio, String chaveS3, String conteudoCsv) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(remetente));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatarioAdmin));
            mimeMessage.setSubject("Relatório semanal de avaliações", "UTF-8");

            MimeMultipart corpoAlternativo = new MimeMultipart("alternative");

            MimeBodyPart partTexto = new MimeBodyPart();
            partTexto.setText(corpoTexto(relatorio, chaveS3), "UTF-8");
            corpoAlternativo.addBodyPart(partTexto);

            MimeBodyPart partHtml = new MimeBodyPart();
            partHtml.setContent(corpoHtml(relatorio, chaveS3), "text/html; charset=UTF-8");
            corpoAlternativo.addBodyPart(partHtml);

            MimeBodyPart partCorpo = new MimeBodyPart();
            partCorpo.setContent(corpoAlternativo);

            MimeBodyPart partAnexo = new MimeBodyPart();
            partAnexo.setDataHandler(new DataHandler(
                    new ByteArrayDataSource(conteudoCsv.getBytes(StandardCharsets.UTF_8), "text/csv")));
            partAnexo.setFileName("relatorio-semanal-%s_%s.csv".formatted(relatorio.dataInicio(), relatorio.dataFim()));

            MimeMultipart mensagemCompleta = new MimeMultipart("mixed");
            mensagemCompleta.addBodyPart(partCorpo);
            mensagemCompleta.addBodyPart(partAnexo);

            mimeMessage.setContent(mensagemCompleta);

            ByteArrayOutputStream saida = new ByteArrayOutputStream();
            mimeMessage.writeTo(saida);
            return saida.toByteArray();
        } catch (MessagingException | IOException e) {
            throw new IllegalStateException("Falha ao montar e-mail com anexo do relatório", e);
        }
    }

    private String corpoTexto(RelatorioSemanalDTO relatorio, String chaveS3) {
        return "Período: " + relatorio.dataInicio() + " a " + relatorio.dataFim() + "\n"
                + "Média de notas: " + String.format(Locale.US, "%.2f", relatorio.mediaNotas()) + "\n"
                + "Total de avaliações: " + relatorio.avaliacoes().size() + "\n"
                + "Relatório completo em anexo (também salvo em: " + chaveS3 + ")";
    }

    private String corpoHtml(RelatorioSemanalDTO relatorio, String chaveS3) {
        return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333333;">
                    <h2 style="color: #2c3e50;">Relatório semanal de avaliações</h2>
                    <table style="border-collapse: collapse;">
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Período</td>
                        <td style="padding: 4px 0;">%s a %s</td>
                      </tr>
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Média de notas</td>
                        <td style="padding: 4px 0;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 4px 12px 4px 0; font-weight: bold;">Total de avaliações</td>
                        <td style="padding: 4px 0;">%d</td>
                      </tr>
                    </table>
                    <p>Relatório completo em anexo (também salvo em <code>%s</code>).</p>
                  </body>
                </html>
                """.formatted(
                relatorio.dataInicio(),
                relatorio.dataFim(),
                String.format(Locale.US, "%.2f", relatorio.mediaNotas()),
                relatorio.avaliacoes().size(),
                chaveS3);
    }
}
