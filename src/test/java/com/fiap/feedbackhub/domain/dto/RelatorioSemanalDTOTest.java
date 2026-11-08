package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class RelatorioSemanalDTOTest {

    @Test
    void deveCalcularMediaQuantidadesEResumoDasAvaliacoes() {
        LocalDate dataInicio = LocalDate.of(2026, 11, 1);
        LocalDate dataFim = LocalDate.of(2026, 11, 2);

        List<Avaliacao> avaliacoes = List.of(
                Avaliacao.novo("Aula ótima", 9, LocalDateTime.of(2026, 11, 1, 10, 0)),
                Avaliacao.novo("Aula ruim", 2, LocalDateTime.of(2026, 11, 1, 11, 0)),
                Avaliacao.novo("Aula mediana", 5, LocalDateTime.of(2026, 11, 2, 9, 0))
        );

        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(avaliacoes, dataInicio, dataFim);

        assertThat(relatorio.mediaNotas()).isCloseTo((9 + 2 + 5) / 3.0, offset(0.001));

        assertThat(relatorio.quantidadePorDia())
                .containsEntry(LocalDate.of(2026, 11, 1), 2L)
                .containsEntry(LocalDate.of(2026, 11, 2), 1L);

        assertThat(relatorio.quantidadePorUrgencia())
                .containsEntry(Urgencia.BAIXA, 1L)
                .containsEntry(Urgencia.ALTA, 1L)
                .containsEntry(Urgencia.MEDIA, 1L);

        assertThat(relatorio.avaliacoes())
                .hasSize(3)
                .extracting(AvaliacaoResumoDTO::descricao)
                .containsExactlyInAnyOrder("Aula ótima", "Aula ruim", "Aula mediana");
    }

    @Test
    void deveRetornarMediaZeroQuandoNaoHaAvaliacoes() {
        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(
                List.of(), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7));

        assertThat(relatorio.mediaNotas()).isZero();
        assertThat(relatorio.quantidadePorDia()).isEmpty();
        assertThat(relatorio.quantidadePorUrgencia()).isEmpty();
        assertThat(relatorio.avaliacoes()).isEmpty();
    }
}
