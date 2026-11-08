package com.fiap.feedbackhub.report;

import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RelatorioSemanalCsvBuilder {

    public String construir(RelatorioSemanalDTO relatorio) {
        StringBuilder csv = new StringBuilder();

        csv.append("Relatorio semanal,").append(relatorio.dataInicio()).append(" a ").append(relatorio.dataFim()).append("\n");
        csv.append("Media de notas,").append(String.format(Locale.US, "%.2f", relatorio.mediaNotas())).append("\n\n");

        csv.append("Quantidade de avaliacoes por dia\n");
        csv.append("data,quantidade\n");
        relatorio.quantidadePorDia().forEach((data, quantidade) ->
                csv.append(data).append(",").append(quantidade).append("\n"));
        csv.append("\n");

        csv.append("Quantidade de avaliacoes por urgencia\n");
        csv.append("urgencia,quantidade\n");
        relatorio.quantidadePorUrgencia().forEach((urgencia, quantidade) ->
                csv.append(urgencia).append(",").append(quantidade).append("\n"));
        csv.append("\n");

        csv.append("Avaliacoes do periodo\n");
        csv.append("descricao,urgencia,dataEnvio\n");
        relatorio.avaliacoes().forEach(avaliacao -> csv
                .append(escaparCsv(avaliacao.descricao())).append(",")
                .append(avaliacao.urgencia()).append(",")
                .append(avaliacao.dataEnvio()).append("\n"));

        return csv.toString();
    }

    private String escaparCsv(String valor) {
        if (valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}
