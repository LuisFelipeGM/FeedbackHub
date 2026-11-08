package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.email.RelatorioSemanalEmailSender;
import com.fiap.feedbackhub.report.RelatorioSemanalCsvBuilder;
import com.fiap.feedbackhub.report.RelatorioSemanalS3Uploader;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioSemanalServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private RelatorioSemanalCsvBuilder csvBuilder;

    @Mock
    private RelatorioSemanalS3Uploader s3Uploader;

    @Mock
    private RelatorioSemanalEmailSender emailSender;

    private RelatorioSemanalService relatorioSemanalService;

    @BeforeEach
    void setup() {
        relatorioSemanalService = new RelatorioSemanalService(avaliacaoRepository, csvBuilder, s3Uploader, emailSender);
    }

    @Test
    void deveConsultarOsSeteDiasAnterioresAgregarESalvarOrelatorio() {
        Avaliacao avaliacao = Avaliacao.novo("Aula boa", 8, LocalDateTime.now());
        when(avaliacaoRepository.buscarPorData(any(LocalDate.class))).thenReturn(List.of(avaliacao));
        when(csvBuilder.construir(any())).thenReturn("csv-gerado");
        when(s3Uploader.salvar(any(), eq("csv-gerado"))).thenReturn("relatorios/chave.csv");

        relatorioSemanalService.gerar();

        verify(avaliacaoRepository, times(7)).buscarPorData(any(LocalDate.class));
        verify(csvBuilder).construir(any());
        verify(s3Uploader).salvar(any(), anyString());
        verify(emailSender).enviar(any(), eq("relatorios/chave.csv"), eq("csv-gerado"));
    }
}
