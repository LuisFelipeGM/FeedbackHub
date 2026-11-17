package com.fiap.feedbackhub.repository;

import com.fiap.feedbackhub.config.LocalStackContainerSingleton;
import com.fiap.feedbackhub.domain.entity.Aula;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AulaRepositoryIT extends BaseRepositoryIT {

    @Test
    void deveBuscarAulaSemeadaPorId() {
        Optional<Aula> aula = aulaRepository.buscarPorId(LocalStackContainerSingleton.AULA_TESTE_ID);

        assertThat(aula).isPresent();
        assertThat(aula.get().getTitulo()).isEqualTo("Aula de Teste");
    }

    @Test
    void naoDeveEncontrarAulaInexistente() {
        Optional<Aula> aula = aulaRepository.buscarPorId("aula-inexistente");

        assertThat(aula).isEmpty();
    }

    @Test
    void deveListarTodasAsAulas() {
        List<Aula> aulas = aulaRepository.listarTodas();

        assertThat(aulas)
                .extracting(Aula::getId)
                .contains(LocalStackContainerSingleton.AULA_TESTE_ID);
    }
}
