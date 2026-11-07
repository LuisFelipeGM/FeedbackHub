package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;
import com.fiap.feedbackhub.messaging.AvaliacaoCriticaPublisher;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final AvaliacaoCriticaPublisher avaliacaoCriticaPublisher;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                             AvaliacaoCriticaPublisher avaliacaoCriticaPublisher) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.avaliacaoCriticaPublisher = avaliacaoCriticaPublisher;
    }

    public Avaliacao registrar(AvaliacaoDTO avaliacaoDTO) {
        Avaliacao avaliacao = Avaliacao.novo(avaliacaoDTO.descricao(), avaliacaoDTO.nota(), LocalDateTime.now());

        avaliacaoRepository.salvar(avaliacao);

        if (avaliacao.getUrgencia() == Urgencia.ALTA) {
            avaliacaoCriticaPublisher.publicar(avaliacao);
        }

        return avaliacao;
    }
}
