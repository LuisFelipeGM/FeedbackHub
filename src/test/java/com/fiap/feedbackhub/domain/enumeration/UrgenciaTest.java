package com.fiap.feedbackhub.domain.enumeration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrgenciaTest {

    @ParameterizedTest
    @CsvSource({
            "0, ALTA",
            "3, ALTA",
            "4, MEDIA",
            "7, MEDIA",
            "8, BAIXA",
            "10, BAIXA"
    })
    void deveClassificarUrgenciaPelaNota(int nota, Urgencia esperado) {
        assertThat(Urgencia.apartirDaNota(nota)).isEqualTo(esperado);
    }

    @Test
    void deveRejeitarNotaForaDoIntervalo() {
        assertThrows(IllegalArgumentException.class, () -> Urgencia.apartirDaNota(-1));
        assertThrows(IllegalArgumentException.class, () -> Urgencia.apartirDaNota(11));
    }
}
