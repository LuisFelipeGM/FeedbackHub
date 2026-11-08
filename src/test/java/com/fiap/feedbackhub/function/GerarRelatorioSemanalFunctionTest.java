package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fiap.feedbackhub.service.RelatorioSemanalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GerarRelatorioSemanalFunctionTest {

    @Mock
    private RelatorioSemanalService relatorioSemanalService;

    private Consumer<ScheduledEvent> gerarRelatorioSemanal;

    @BeforeEach
    void setup() {
        gerarRelatorioSemanal = new GerarRelatorioSemanalFunction().gerarRelatorioSemanal(relatorioSemanalService);
    }

    @Test
    void deveAcionarAGeracaoDoRelatorioQuandoEventoDisparar() {
        gerarRelatorioSemanal.accept(new ScheduledEvent());

        verify(relatorioSemanalService).gerar();
    }
}
