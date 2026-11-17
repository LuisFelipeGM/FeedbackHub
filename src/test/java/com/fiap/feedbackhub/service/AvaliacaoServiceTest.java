package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.domain.entity.Aula;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.messaging.AvaliacaoCriticaPublisher;
import com.fiap.feedbackhub.repository.AulaRepository;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private AvaliacaoCriticaPublisher avaliacaoCriticaPublisher;

    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void setup() {
        avaliacaoService = new AvaliacaoService(avaliacaoRepository, aulaRepository, avaliacaoCriticaPublisher);
        lenient().when(aulaRepository.buscarPorId(eq("aula-01"))).thenReturn(Optional.of(new Aula()));
    }

    @Test
    void deveSempreSalvarAAvaliacao() {
        avaliacaoService.registrar(new AvaliacaoDTO(7, "Aula ok", "aula-01"));

        verify(avaliacaoRepository).salvar(any());
    }

    @Test
    void devePublicarNaFilaQuandoUrgenciaForAlta() {
        avaliacaoService.registrar(new AvaliacaoDTO(2, "Aula muito ruim", "aula-01"));

        ArgumentCaptor<Avaliacao> captor = ArgumentCaptor.forClass(Avaliacao.class);
        verify(avaliacaoCriticaPublisher).publicar(captor.capture());
        assertThat(captor.getValue().getNota()).isEqualTo(2);
    }

    @Test
    void naoDevePublicarNaFilaQuandoUrgenciaForMedia() {
        avaliacaoService.registrar(new AvaliacaoDTO(5, "Aula mediana", "aula-01"));

        verify(avaliacaoCriticaPublisher, never()).publicar(any());
    }

    @Test
    void naoDevePublicarNaFilaQuandoUrgenciaForBaixa() {
        avaliacaoService.registrar(new AvaliacaoDTO(9, "Aula excelente", "aula-01"));

        verify(avaliacaoCriticaPublisher, never()).publicar(any());
    }

    @Test
    void deveLancarExcecaoQuandoAulaNaoExiste() {
        when(aulaRepository.buscarPorId("aula-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.registrar(new AvaliacaoDTO(7, "Aula ok", "aula-inexistente")))
                .isInstanceOf(NoSuchElementException.class);

        verify(avaliacaoRepository, never()).salvar(any());
    }
}
