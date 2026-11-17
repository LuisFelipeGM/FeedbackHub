package com.fiap.feedbackhub.cognito;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;

@Component
public class AtribuicaoGrupoAlunoService {

    private static final String GRUPO_ALUNO = "ALUNO";

    private final CognitoIdentityProviderClient cognitoClient;

    public AtribuicaoGrupoAlunoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public void atribuir(String userPoolId, String userName) {
        try {
            cognitoClient.adminAddUserToGroup(AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userName)
                    .groupName(GRUPO_ALUNO)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
