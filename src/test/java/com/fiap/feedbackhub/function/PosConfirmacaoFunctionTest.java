package com.fiap.feedbackhub.function;

import com.fiap.feedbackhub.cognito.AtribuicaoGrupoAlunoService;
import com.fiap.feedbackhub.domain.dto.PosConfirmacaoEventoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PosConfirmacaoFunctionTest {

    @Mock
    private AtribuicaoGrupoAlunoService atribuicaoGrupoAlunoService;

    private Function<PosConfirmacaoEventoDTO, PosConfirmacaoEventoDTO> posConfirmacao;

    @BeforeEach
    void setup() {
        posConfirmacao = new PosConfirmacaoFunction().posConfirmacao(atribuicaoGrupoAlunoService);
    }

    @Test
    void deveAtribuirGrupoAlunoERetornarOMesmoEvento() {
        PosConfirmacaoEventoDTO event = new PosConfirmacaoEventoDTO(
                "us-east-1_abc123",
                "a1b2c3d4",
                new PosConfirmacaoEventoDTO.RequestDTO(Map.of("email", "aluno@exemplo.com")),
                Map.of());

        PosConfirmacaoEventoDTO resultado = posConfirmacao.apply(event);

        verify(atribuicaoGrupoAlunoService).atribuir("us-east-1_abc123", "a1b2c3d4");
        assertThat(resultado).isSameAs(event);
    }
}
