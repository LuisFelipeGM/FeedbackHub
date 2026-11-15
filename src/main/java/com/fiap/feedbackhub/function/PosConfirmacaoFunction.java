package com.fiap.feedbackhub.function;

import com.fiap.feedbackhub.cognito.AtribuicaoGrupoAlunoService;
import com.fiap.feedbackhub.domain.dto.PosConfirmacaoEventoDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class PosConfirmacaoFunction {

    @Bean
    public Function<PosConfirmacaoEventoDTO, PosConfirmacaoEventoDTO> posConfirmacao(
            AtribuicaoGrupoAlunoService atribuicaoGrupoAlunoService) {

        return event -> {
            atribuicaoGrupoAlunoService.atribuir(event.userPoolId(), event.userName());
            return event;
        };
    }
}
