package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;

public record NotificacaoCriticaDTO(
        String descricao,
        Urgencia urgencia,
        String dataEnvio
) {

    public static NotificacaoCriticaDTO de(Avaliacao avaliacao) {
        return new NotificacaoCriticaDTO(
                avaliacao.getDescricao(),
                avaliacao.getUrgencia(),
                avaliacao.getDataHoraEnvio());
    }
}
