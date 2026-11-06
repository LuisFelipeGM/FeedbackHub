package com.fiap.feedbackhub.support;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

public final class ValidacaoSupport {

    private ValidacaoSupport() {
    }

    public static <T> String mensagemErro(Set<ConstraintViolation<T>> violacoes) {
        return violacoes.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
    }
}
