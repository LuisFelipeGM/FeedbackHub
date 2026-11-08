package com.fiap.feedbackhub.report;

import com.fiap.feedbackhub.domain.dto.AvaliacaoResumoDTO;
import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioSemanalCsvBuilderTest {

    private final RelatorioSemanalCsvBuilder csvBuilder = new RelatorioSemanalCsvBuilder();

    @Test
    void deveMontarCsvComTodasAsSecoes() {
        Map<LocalDate, Long> quantidadePorDia = new TreeMap<>();
        quantidadePorDia.put(LocalDate.of(2026, 11, 1), 2L);

        Map<Urgencia, Long> quantidadePorUrgencia = new EnumMap<>(Urgencia.class);
        quantidadePorUrgencia.put(Urgencia.ALTA, 1L);

        RelatorioSemanalDTO relatorio = new RelatorioSemanalDTO(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 7),
                6.5,
                quantidadePorDia,
                quantidadePorUrgencia,
                List.of(new AvaliacaoResumoDTO("Aula ruim", Urgencia.ALTA, "2026-11-01T10:00:00")));

        String csv = csvBuilder.construir(relatorio);

        assertThat(csv)
                .contains("2026-11-01 a 2026-11-07")
                .contains("Media de notas,6.50")
                .contains("2026-11-01,2")
                .contains("ALTA,1")
                .contains("Aula ruim,ALTA,2026-11-01T10:00:00");
    }

    @Test
    void deveEscaparDescricaoComVirgulaOuAspas() {
        RelatorioSemanalDTO relatorio = new RelatorioSemanalDTO(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 7),
                5.0,
                Map.of(),
                Map.of(),
                List.of(new AvaliacaoResumoDTO("Aula boa, mas \"longa\"", Urgencia.MEDIA, "2026-11-01T10:00:00")));

        String csv = csvBuilder.construir(relatorio);

        assertThat(csv).contains("\"Aula boa, mas \"\"longa\"\"\",MEDIA,2026-11-01T10:00:00");
    }
}
