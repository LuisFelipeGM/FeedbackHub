package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import com.fiap.feedbackhub.messaging.AvaliacaoCriticaPublisher;
import com.fiap.feedbackhub.repository.AulaRepository;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AulaRepository aulaRepository;
    private final AvaliacaoCriticaPublisher avaliacaoCriticaPublisher;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                             AulaRepository aulaRepository,
                             AvaliacaoCriticaPublisher avaliacaoCriticaPublisher) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.aulaRepository = aulaRepository;
        this.avaliacaoCriticaPublisher = avaliacaoCriticaPublisher;
    }

    public Avaliacao registrar(AvaliacaoDTO avaliacaoDTO) {
        if (aulaRepository.buscarPorId(avaliacaoDTO.aulaId()).isEmpty()) {
            throw new NoSuchElementException("Aula não encontrada: " + avaliacaoDTO.aulaId());
        }

        Avaliacao avaliacao = Avaliacao.novo(
                avaliacaoDTO.descricao(), avaliacaoDTO.nota(), avaliacaoDTO.aulaId(), LocalDateTime.now());

        avaliacaoRepository.salvar(avaliacao);

        if (avaliacao.getUrgencia() == Urgencia.ALTA) {
            avaliacaoCriticaPublisher.publicar(avaliacao);
        }

        return avaliacao;
    }
}
