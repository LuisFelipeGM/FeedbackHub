package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.domain.enumeration.Urgencia;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public record RelatorioSemanalDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        Double mediaNotas,
        Map<LocalDate, Long> quantidadePorDia,
        Map<Urgencia, Long> quantidadePorUrgencia,
        List<AvaliacaoResumoDTO> avaliacoes
) {

    public static RelatorioSemanalDTO calcular(List<Avaliacao> avaliacoes, LocalDate dataInicio, LocalDate dataFim) {
        double mediaNotas = avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0.0);

        Map<LocalDate, Long> quantidadePorDia = avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        avaliacao -> LocalDate.parse(avaliacao.getData()),
                        TreeMap::new,
                        Collectors.counting()));

        Map<Urgencia, Long> quantidadePorUrgencia = avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        Avaliacao::getUrgencia,
                        () -> new EnumMap<>(Urgencia.class),
                        Collectors.counting()));

        List<AvaliacaoResumoDTO> resumo = avaliacoes.stream()
                .map(AvaliacaoResumoDTO::de)
                .toList();

        return new RelatorioSemanalDTO(dataInicio, dataFim, mediaNotas, quantidadePorDia, quantidadePorUrgencia, resumo);
    }
}
