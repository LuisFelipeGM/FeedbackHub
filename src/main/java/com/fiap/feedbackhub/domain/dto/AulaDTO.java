package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Aula;

public record AulaDTO(
        String id,
        String titulo
) {

    public static AulaDTO de(Aula aula) {
        return new AulaDTO(aula.getId(), aula.getTitulo());
    }
}
