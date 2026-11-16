# FeedbackHub

Plataforma serverless de feedback de cursos — Tech Challenge Fase 4 (Pós-Tech FIAP, Arquitetura e Desenvolvimento Java).

## Autenticação e Autorização (Amazon Cognito)

A plataforma usa um Amazon Cognito User Pool com dois grupos: `ALUNO` e `ADMIN`.

> Essa exigência de autenticação/autorização não está no enunciado original do desafio — foi um requisito adicional que o grupo definiu para a Fase 6, aprovado pelo professor.

### Alunos (`ALUNO`)

Alunos se cadastram sozinhos (self-sign-up) no User Pool. A confirmação do cadastro (verificação de e-mail) dispara automaticamente um trigger Lambda (`PosConfirmacaoFunction`) que adiciona o usuário ao grupo `ALUNO` via `AdminAddUserToGroup` — não há nenhuma etapa manual para esse caso.

O endpoint `POST /avaliacao` exige um token JWT do Cognito (via Authorizer do API Gateway) **e** que o usuário pertença ao grupo `ALUNO`; qualquer outro usuário autenticado recebe `403`.

### Administradores (`ADMIN`)

Não existe fluxo de self-sign-up para administradores — o cadastro é manual, via AWS CLI, usando o `UserPoolId` exposto como output da stack CDK (`cdk deploy` imprime `UserPoolId` e `UserPoolClientId`).

1. Criar o usuário (sem enviar e-mail de convite):

```bash
aws cognito-idp admin-create-user \
  --user-pool-id <USER_POOL_ID> \
  --username admin@exemplo.com \
  --user-attributes Name=email,Value=admin@exemplo.com Name=email_verified,Value=true \
  --message-action SUPPRESS
```

2. Definir uma senha permanente diretamente (evita o desafio `NEW_PASSWORD_REQUIRED` do primeiro login):

```bash
aws cognito-idp admin-set-user-password \
  --user-pool-id <USER_POOL_ID> \
  --username admin@exemplo.com \
  --password "SenhaForte123!" \
  --permanent
```

3. Adicionar o usuário ao grupo `ADMIN`:

```bash
aws cognito-idp admin-add-user-to-group \
  --user-pool-id <USER_POOL_ID> \
  --username admin@exemplo.com \
  --group-name ADMIN
```

4. (Opcional) Testar o login e inspecionar o token gerado:

```bash
aws cognito-idp initiate-auth \
  --client-id <USER_POOL_CLIENT_ID> \
  --auth-flow USER_PASSWORD_AUTH \
  --auth-parameters USERNAME=admin@exemplo.com,PASSWORD="SenhaForte123!"
```

O `IdToken` retornado deve conter a claim `cognito:groups` com o valor `ADMIN`.

> Hoje nenhum endpoint da API exige explicitamente o grupo `ADMIN` — o administrador recebe as notificações críticas e o relatório semanal por e-mail (SES), fora do fluxo autenticado da API. O grupo existe para representar esse papel e para uma eventual rota administrativa futura.
