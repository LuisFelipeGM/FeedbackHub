package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;

public record AvaliacaoResumoDTO(
        String descricao,
        Urgencia urgencia,
        String dataEnvio
) {

    public static AvaliacaoResumoDTO de(Avaliacao avaliacao) {
        return new AvaliacaoResumoDTO(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataHoraEnvio());
    }
}
