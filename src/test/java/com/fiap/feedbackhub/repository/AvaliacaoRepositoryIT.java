package com.fiap.feedbackhub.repository;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvaliacaoRepositoryIT extends BaseRepositoryIT {

    @Test
    void deveSalvarEBuscarAvaliacaoPorData() {
        LocalDateTime momento = LocalDateTime.of(2026, 11, 4, 10, 15, 30);
        Avaliacao avaliacao = Avaliacao.novo("Aula muito boa", 9, momento);

        avaliacaoRepository.salvar(avaliacao);

        List<Avaliacao> encontradas = avaliacaoRepository.buscarPorData(LocalDate.of(2026, 11, 4));

        assertThat(encontradas).hasSize(1);
        Avaliacao salva = encontradas.getFirst();
        assertThat(salva.getDescricao()).isEqualTo("Aula muito boa");
        assertThat(salva.getNota()).isEqualTo(9);
        assertThat(salva.getUrgencia().name()).isEqualTo("BAIXA");
    }

    @Test
    void naoDeveEncontrarAvaliacoesEmDataSemRegistros() {
        List<Avaliacao> encontradas = avaliacaoRepository.buscarPorData(LocalDate.of(2000, 1, 1));
        assertThat(encontradas).isEmpty();
    }
}
