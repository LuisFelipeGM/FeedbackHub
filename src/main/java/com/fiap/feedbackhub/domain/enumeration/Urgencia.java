package com.fiap.feedbackhub.domain.enumeration;

public enum Urgencia {

    ALTA,
    MEDIA,
    BAIXA;

    public static Urgencia apartirDaNota(int nota) {
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10: " + nota);
        }
        if (nota <= 3) {
            return ALTA;
        }
        if (nota <= 7) {
            return MEDIA;
        }
        return BAIXA;
    }
}
