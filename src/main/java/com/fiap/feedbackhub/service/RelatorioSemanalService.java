package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.domain.dto.RelatorioSemanalDTO;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.email.RelatorioSemanalEmailSender;
import com.fiap.feedbackhub.report.RelatorioSemanalCsvBuilder;
import com.fiap.feedbackhub.report.RelatorioSemanalS3Uploader;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RelatorioSemanalService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final RelatorioSemanalCsvBuilder csvBuilder;
    private final RelatorioSemanalS3Uploader s3Uploader;
    private final RelatorioSemanalEmailSender emailSender;

    public RelatorioSemanalService(
            AvaliacaoRepository avaliacaoRepository,
            RelatorioSemanalCsvBuilder csvBuilder,
            RelatorioSemanalS3Uploader s3Uploader,
            RelatorioSemanalEmailSender emailSender
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.csvBuilder = csvBuilder;
        this.s3Uploader = s3Uploader;
        this.emailSender = emailSender;
    }

    public void gerar() {
        LocalDate dataFim = LocalDate.now().minusDays(1);
        LocalDate dataInicio = dataFim.minusDays(6);

        List<Avaliacao> avaliacoesDaSemana = buscarAvaliacoesDoPeriodo(dataInicio, dataFim);

        RelatorioSemanalDTO relatorio = RelatorioSemanalDTO.calcular(avaliacoesDaSemana, dataInicio, dataFim);
        String csv = csvBuilder.construir(relatorio);
        String chaveS3 = s3Uploader.salvar(relatorio, csv);
        emailSender.enviar(relatorio, chaveS3, csv);
    }

    private List<Avaliacao> buscarAvaliacoesDoPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        for (LocalDate data = dataInicio; !data.isAfter(dataFim); data = data.plusDays(1)) {
            avaliacoes.addAll(avaliacaoRepository.buscarPorData(data));
        }
        return avaliacoes;
    }
}
