package com.fiap.feedbackhub.cognito;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtribuicaoGrupoAlunoServiceTest {

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private AtribuicaoGrupoAlunoService atribuicaoGrupoAlunoService;

    @BeforeEach
    void setup() {
        atribuicaoGrupoAlunoService = new AtribuicaoGrupoAlunoService(cognitoClient);
    }

    @Test
    void deveAdicionarUsuarioAoGrupoAluno() {
        atribuicaoGrupoAlunoService.atribuir("us-east-1_abc123", "a1b2c3d4");

        ArgumentCaptor<AdminAddUserToGroupRequest> captor = ArgumentCaptor.forClass(AdminAddUserToGroupRequest.class);
        verify(cognitoClient).adminAddUserToGroup(captor.capture());

        AdminAddUserToGroupRequest requisicao = captor.getValue();
        assertThat(requisicao.userPoolId()).isEqualTo("us-east-1_abc123");
        assertThat(requisicao.username()).isEqualTo("a1b2c3d4");
        assertThat(requisicao.groupName()).isEqualTo("ALUNO");
    }

    @Test
    void deveEngolirExcecaoQuandoCognitoFalhar() {
        when(cognitoClient.adminAddUserToGroup(any(AdminAddUserToGroupRequest.class)))
                .thenThrow(new RuntimeException("falha simulada"));

        assertThatCode(() -> atribuicaoGrupoAlunoService.atribuir("us-east-1_abc123", "a1b2c3d4"))
                .doesNotThrowAnyException();
    }
}
