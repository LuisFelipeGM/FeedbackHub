package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Aula;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

class RelatorioSemanalDTOTest {

    @Test
    void deveCalcularMediaQuantidadesEResumoDasAvaliacoes() {
        LocalDate dataInicio = LocalDate.of(2026, 11, 1);
        LocalDate dataFim = LocalDate.of(2026, 11, 2);

        List<Avaliacao> avaliacoes = List.of(
                Avaliacao.novo("Aula ótima", 9, "aula-01", LocalDateTime.of(2026, 11, 1, 10, 0)),
                Avaliacao.novo("Aula ruim", 2, "aula-02", LocalDateTime.of(2026, 11, 1, 11, 0)),
                Avaliacao.novo("Aula mediana", 5, "aula-01", LocalDateTime.of(2026, 11, 2, 9, 0))
        );

        Map<String, Aula> aulasPorId = Map.of(
                "aula-01", aula("aula-01", "Arquitetura Serverless"),
                "aula-02", aula("aula-02", "Mensageria com SQS"));

        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(avaliacoes, aulasPorId, dataInicio, dataFim);

        assertThat(relatorio.mediaNotas()).isCloseTo((9 + 2 + 5) / 3.0, offset(0.001));

        assertThat(relatorio.quantidadePorDia())
                .containsEntry(LocalDate.of(2026, 11, 1), 2L)
                .containsEntry(LocalDate.of(2026, 11, 2), 1L);

        assertThat(relatorio.quantidadePorUrgencia())
                .containsEntry(Urgencia.BAIXA, 1L)
                .containsEntry(Urgencia.ALTA, 1L)
                .containsEntry(Urgencia.MEDIA, 1L);

        assertThat(relatorio.quantidadePorAula())
                .containsEntry("Arquitetura Serverless", 2L)
                .containsEntry("Mensageria com SQS", 1L);

        assertThat(relatorio.mediaNotasPorAula().get("Arquitetura Serverless")).isCloseTo((9 + 5) / 2.0, offset(0.001));
        assertThat(relatorio.mediaNotasPorAula().get("Mensageria com SQS")).isCloseTo(2.0, offset(0.001));

        assertThat(relatorio.avaliacoes())
                .hasSize(3)
                .extracting(AvaliacaoResumoDTO::descricao)
                .containsExactlyInAnyOrder("Aula ótima", "Aula ruim", "Aula mediana");

        assertThat(relatorio.avaliacoes())
                .extracting(AvaliacaoResumoDTO::aulaTitulo)
                .containsOnly("Arquitetura Serverless", "Mensageria com SQS");
    }

    @Test
    void deveUsarAulaIdComoTituloQuandoAulaNaoForEncontrada() {
        List<Avaliacao> avaliacoes = List.of(
                Avaliacao.novo("Aula sem cadastro", 7, "aula-removida", LocalDateTime.of(2026, 11, 1, 10, 0)));

        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(
                avaliacoes, Map.of(), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7));

        assertThat(relatorio.avaliacoes().getFirst().aulaTitulo()).isEqualTo("aula-removida");
        assertThat(relatorio.quantidadePorAula()).containsEntry("aula-removida", 1L);
    }

    @Test
    void deveRetornarMediaZeroQuandoNaoHaAvaliacoes() {
        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(
                List.of(), Map.of(), LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 7));

        assertThat(relatorio.mediaNotas()).isZero();
        assertThat(relatorio.quantidadePorDia()).isEmpty();
        assertThat(relatorio.quantidadePorUrgencia()).isEmpty();
        assertThat(relatorio.quantidadePorAula()).isEmpty();
        assertThat(relatorio.mediaNotasPorAula()).isEmpty();
        assertThat(relatorio.avaliacoes()).isEmpty();
    }

    private Aula aula(String id, String titulo) {
        Aula aula = new Aula();
        aula.setId(id);
        aula.setTitulo(titulo);
        return aula;
    }
}
