package com.fiap.feedbackhub.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoDTO(

        @NotNull(message = "nota é obrigatória")
        @Min(value = 0, message = "nota deve ser no mínimo 0")
        @Max(value = 10, message = "nota deve ser no máximo 10")
        Integer nota,

        @NotBlank(message = "descricao é obrigatória")
        String descricao
) {
}
