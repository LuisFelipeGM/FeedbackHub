package com.fiap.feedbackhub.domain.dto;

import com.fiap.feedbackhub.domain.entity.Aula;
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
        Map<String, Long> quantidadePorAula,
        Map<String, Double> mediaNotasPorAula,
        List<AvaliacaoResumoDTO> avaliacoes
) {

    public static RelatorioSemanalDTO calcular(
            List<Avaliacao> avaliacoes, Map<String, Aula> aulasPorId, LocalDate dataInicio, LocalDate dataFim) {

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

        Map<String, Long> quantidadePorAula = avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        avaliacao -> tituloAula(avaliacao, aulasPorId),
                        TreeMap::new,
                        Collectors.counting()));

        Map<String, Double> mediaNotasPorAula = avaliacoes.stream()
                .collect(Collectors.groupingBy(
                        avaliacao -> tituloAula(avaliacao, aulasPorId),
                        TreeMap::new,
                        Collectors.averagingInt(Avaliacao::getNota)));

        List<AvaliacaoResumoDTO> resumo = avaliacoes.stream()
                .map(avaliacao -> AvaliacaoResumoDTO.de(avaliacao, tituloAula(avaliacao, aulasPorId)))
                .toList();

        return new RelatorioSemanalDTO(
                dataInicio, dataFim, mediaNotas, quantidadePorDia, quantidadePorUrgencia,
                quantidadePorAula, mediaNotasPorAula, resumo);
    }

    private static String tituloAula(Avaliacao avaliacao, Map<String, Aula> aulasPorId) {
        Aula aula = aulasPorId.get(avaliacao.getAulaId());
        return aula != null ? aula.getTitulo() : avaliacao.getAulaId();
    }
}
