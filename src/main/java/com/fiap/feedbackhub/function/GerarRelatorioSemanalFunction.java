package com.fiap.feedbackhub.function;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.fiap.feedbackhub.service.RelatorioSemanalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class GerarRelatorioSemanalFunction {

    @Bean
    public Consumer<ScheduledEvent> gerarRelatorioSemanal(RelatorioSemanalService relatorioSemanalService) {
        return event -> relatorioSemanalService.gerar();
    }
}
