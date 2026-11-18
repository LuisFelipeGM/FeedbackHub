# 📝 FeedbackHub

Projeto do **Tech Challenge da 4ª fase** da Pós-Graduação em **Arquitetura e Desenvolvimento Java** (módulo Serverless Strategist).

O FeedbackHub é uma plataforma de feedback de aulas construída **inteiramente serverless** na AWS: avaliações são recebidas via API, notificações críticas e o relatório semanal são processados de forma assíncrona, e toda a infraestrutura é provisionada como código (CDK) e implantada automaticamente via pipeline.

👨‍💻 Desenvolvido por: **LuisFelipeGM - RM: 371055**

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-green)
![Spring Cloud Function](https://img.shields.io/badge/Spring%20Cloud%20Function-4.2-6DB33F)
![AWS Lambda](https://img.shields.io/badge/AWS-Lambda-FF9900?logo=awslambda&logoColor=white)
![API Gateway](https://img.shields.io/badge/AWS-API%20Gateway-FF9900?logo=amazonapigateway&logoColor=white)
![DynamoDB](https://img.shields.io/badge/AWS-DynamoDB-4053D6?logo=amazondynamodb&logoColor=white)
![SQS](https://img.shields.io/badge/AWS-SQS-FF4F8B?logo=amazonsqs&logoColor=white)
![S3](https://img.shields.io/badge/AWS-S3-569A31?logo=amazons3&logoColor=white)
![SES](https://img.shields.io/badge/AWS-SES-DD344C)
![Cognito](https://img.shields.io/badge/AWS-Cognito-DD344C?logo=amazoncognito&logoColor=white)
![EventBridge](https://img.shields.io/badge/AWS-EventBridge-FF4F8B)
![CloudWatch](https://img.shields.io/badge/AWS-CloudWatch-FF4F8B)
![CDK](https://img.shields.io/badge/IaC-AWS%20CDK%20(Java)-FF9900)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-LocalStack-2496ED)

---
## Entregavéis:
📄 [4 Fase - FeedbackHub - Tech Challenge.pdf](4%20Fase%20-%20FeedbackHub%20-%20Tech%20Challenge.pdf)
---
🎥 [Vídeo de Apresentação](https://www.youtube.com/watch?v=5UEcnk05u9k)
---

# 🎯 Visão Geral

O FeedbackHub permite que alunos avaliem aulas e dispara automaticamente ações de acordo com a gravidade do feedback.

A plataforma permite:

- ✅ Cadastro de avaliações de aula (nota + descrição + aula avaliada)
- ✅ Classificação automática de urgência pela nota (ALTA / MEDIA / BAIXA)
- ✅ Notificação crítica automática por e-mail quando a urgência é ALTA
- ✅ Relatório semanal automático (média, contagem por dia/urgência/aula) em CSV, salvo no S3 e enviado por e-mail
- ✅ Listagem de aulas (cadastro fixo, sem CRUD exposto)
- ✅ Autenticação e autorização via Amazon Cognito, com papéis ALUNO e ADMIN
- ✅ Monitoramento automático (alarmes de erro por função + fila morta) com notificação ao admin
- ✅ Deploy de toda a infraestrutura como código (CDK), com pipeline de CI/CD

---

# 🏛️ Arquitetura

Arquitetura 100% serverless: nenhum servidor é provisionado ou gerenciado manualmente.

```
                      ┌────────────────────────────┐
                      │      Amazon Cognito        │
                      │  User Pool (grupos:        │
                      │  ALUNO / ADMIN)            │
                      └──────────────┬─────────────┘
                                     │ valida o token (Authorizer)
                                     ▼
                      ┌────────────────────────────┐
                      │   API Gateway (REST)       │
                      └──────────────┬─────────────┘
                     ┌───────────────┴────────────────┐
                     ▼                                ▼
          POST /avaliacao (grupo ALUNO)        GET /aulas (autenticado)
     ┌──────────────────────────┐          ┌──────────────────────┐
     │ ReceberAvaliacaoFunction │          │  ListarAulasFunction │
     └─────────────┬────────────┘          └───────────┬──────────┘
                   ▼                                   ▼
       ┌─────────────────────────┐             ┌──────────────────────┐
       │ DynamoDB: Avaliacoes    │             │ DynamoDB: Aulas      │
       │ (escrita)               │◀──consulta──┤ (leitura, dado fixo) │
       └─────────────┬───────────┘             └──────────────────────┘
                     │ se Urgencia == ALTA
                     ▼
          ┌─────────────────────┐
          │ SQS notificacoes    │
          │ criticas (+ DLQ)    │
          └─────────────────────┘
```

## Lambdas (5, cada uma com responsabilidade única)

| Função | Disparo | Responsabilidade |
|---|---|---|
| `ReceberAvaliacaoFunction` | API Gateway `POST /avaliacao` | Valida o payload, confirma que quem chama é do grupo `ALUNO`, confirma que a aula existe, persiste a avaliação e publica na fila de críticos se a urgência for ALTA |
| `ListarAulasFunction` | API Gateway `GET /aulas` | Lista as aulas cadastradas (dado fixo) |
| `NotificarCriticoFunction` | SQS (`sqs-notificacoes-criticas`) | Consome a fila e envia e-mail de notificação crítica ao admin via SES |
| `GerarRelatorioSemanalFunction` | EventBridge (cron semanal) | Agrega as avaliações dos últimos 7 dias (por dia, urgência e aula), gera um CSV, salva no S3 e envia por e-mail |
| `PosConfirmacaoFunction` | Cognito (trigger Post Confirmation) | Adiciona o aluno recém-confirmado ao grupo `ALUNO` |

Todas compartilham a mesma imagem de container (ver seção [Empacotamento](#-empacotamento-docker)) e persistência (DynamoDB).

---

# 🛠️ Tecnologias

## Backend
- Java 21
- Spring Boot 4.1
- Spring Cloud Function (+ adapter AWS Lambda)
- Hibernate Validator (Bean Validation)

## AWS (Serverless)
- AWS Lambda (imagem de container)
- Amazon API Gateway (REST API)
- Amazon DynamoDB (Enhanced Client)
- Amazon SQS (+ Dead Letter Queue)
- Amazon S3
- Amazon SES (e-mails com anexo, via MIME)
- Amazon Cognito (User Pools, Groups, Triggers)
- Amazon EventBridge (agendamento)
- Amazon CloudWatch (Alarms) + Amazon SNS (notificação operacional)

## Infraestrutura como Código
- AWS CDK (Java)

## Testes
- JUnit 5
- Mockito
- Testcontainers + LocalStack

## Ferramentas
- Lombok
- Gradle (+ plugin Shadow, para gerar o uber-jar da Lambda)
- Jakarta Mail (montagem de e-mail MIME com anexo)

## DevOps
- Docker
- GitHub Actions (CI na PR, CD automático na `main`, autenticação via OIDC)

---

# 🐳 Empacotamento (Docker)

Diferente de uma aplicação com containers de vida longa, aqui o Docker empacota cada Lambda como **imagem de container** (`public.ecr.aws/lambda/java:21`). As 5 funções compartilham a **mesma imagem**: o `Dockerfile` builda um único uber-jar (via `./gradlew shadowJar`) com todo o código da aplicação, e a própria infraestrutura decide qual função cada Lambda executa através da variável de ambiente `SPRING_CLOUD_FUNCTION_DEFINITION` (`receberAvaliacao`, `listarAulas`, `notificarCritico`, `gerarRelatorioSemanal` ou `posConfirmacao`).

```dockerfile
FROM eclipse-temurin:21-jdk AS build
# builda o uber-jar com ./gradlew shadowJar

FROM public.ecr.aws/lambda/java:21
# copia o uber-jar e usa o FunctionInvoker do Spring Cloud Function como handler
```

---

# 🏗️ Infraestrutura como Código (CDK)

## Por que CDK, e não CloudFormation puro ou Terraform

A decisão central foi escrever a infraestrutura em Java, a mesma linguagem da aplicação. Em vez de descrever cada recurso em YAML/JSON (CloudFormation puro) ou em uma linguagem própria (Terraform), o CDK oferece construtores de alto nível (L2) que já encapsulam boas práticas da AWS. Por exemplo, `Table.Builder` já resolve detalhes que em CloudFormation puro precisariam ser escritos manualmente, e `UserPool.Builder` já expõe política de senha, verificação de atributos e grupos sem exigir conhecer a estrutura inteira do recurso `AWS::Cognito::UserPool`.

O CDK não substitui o CloudFormation, ele gera um template CloudFormation. Toda a maturidade de rollback automático em falha, detecção de drift e change sets que já existe no CloudFormation continua valendo.

## Como funciona

1. `FeedbackhubApp` (`infra/src/main/java/.../FeedbackhubApp.java`) cria a `FeedbackhubStack`, que no construtor declara todos os recursos (tabelas, filas, Lambdas, API Gateway, Cognito, etc.) usando a API Java do CDK.
2. `cdk synth` executa esse programa Java (via Gradle) e converte a árvore de construtores em um template CloudFormation.
3. `cdk deploy` builda e publica a imagem Docker das Lambdas no ECR (ver seção [Empacotamento](#-empacotamento-docker)), envia o template gerado para o CloudFormation, e o CloudFormation provisiona ou atualiza os recursos reais na conta AWS.
4. `cdk bootstrap` roda uma vez por conta/região e cria o bucket S3 e os papéis IAM que o próprio CDK usa para funcionar. Não é um recurso da nossa aplicação, é infraestrutura de apoio do CDK.

## Por que o Node.js aparece como pré-requisito

Toda a infraestrutura é descrita em Java, mas a ferramenta de linha de comando do CDK (o comando `cdk`, usado para `synth`, `deploy` e `bootstrap`) é distribuída como um pacote Node.js (`npm install -g aws-cdk`). Esse CLI é só o orquestrador: ele lê o `cdk.json`, que diz como executar o nosso programa Java (`"app": "./gradlew -q run"`), recebe de volta o template CloudFormation gerado por ele e faz as chamadas às APIs da AWS para aplicar esse template. O Node.js não participa da descrição da infraestrutura, apenas da execução do processo de synth e deploy.

---

# 🚀 Execução

## 📋 Pré-requisitos

- Conta AWS com credenciais configuradas (`aws configure`)
- Node.js 22+ e AWS CDK CLI (`npm install -g aws-cdk`)
- Docker
- Java 21

## ☁️ Deploy na AWS (recomendado)

```bash
cd infra
cdk bootstrap
cdk deploy
```

A CDK builda a imagem Docker, publica no ECR, cria toda a infraestrutura (API Gateway, as 5 Lambdas, DynamoDB, SQS, S3, SES, Cognito, EventBridge, CloudWatch) e expõe os outputs: `UrlApi`, `UserPoolId` e `UserPoolClientId`.

## 🔄 Deploy automático (CI/CD)

Push/merge na `main` dispara o workflow `deploy.yml` (GitHub Actions), que roda os testes, autentica na AWS via **OIDC** (sem credenciais fixas) e executa `cdk deploy --require-approval never`.

---

# 🔐 Autenticação e Autorização

A plataforma usa um Amazon Cognito User Pool com dois grupos: `ALUNO` e `ADMIN`.

> Essa exigência de autenticação/autorização não está no enunciado original do desafio, foi um requisito adicional definido para este projeto, aprovado pelo professor.

## Alunos (`ALUNO`)

Alunos se cadastram sozinhos (self-sign-up) no User Pool. A confirmação do cadastro (verificação de e-mail) dispara automaticamente a `PosConfirmacaoFunction`, que adiciona o usuário ao grupo `ALUNO` via `AdminAddUserToGroup`, não há nenhuma etapa manual.

O endpoint `POST /avaliacao` exige um token JWT do Cognito (validado pelo Authorizer do API Gateway) **e** que o usuário pertença ao grupo `ALUNO`, qualquer outro usuário autenticado recebe `403`.

## Administradores (`ADMIN`)

Não existe self-sign-up para administradores, o cadastro é manual, via Console ou AWS CLI, usando o `UserPoolId` exposto como output da stack CDK.

1. Criar o usuário (Console: *Create user*, com senha temporária ou CLI com `admin-create-user --message-action SUPPRESS`).
2. Definir uma senha permanente (evita o desafio `NEW_PASSWORD_REQUIRED` no primeiro login):
   ```bash
   aws cognito-idp admin-set-user-password \
     --user-pool-id <USER_POOL_ID> \
     --username admin@exemplo.com \
     --password "SenhaForte123!" \
     --permanent
   ```
3. Adicionar ao grupo `ADMIN`:
   ```bash
   aws cognito-idp admin-add-user-to-group \
     --user-pool-id <USER_POOL_ID> \
     --username admin@exemplo.com \
     --group-name ADMIN
   ```

> Hoje nenhum endpoint da API exige explicitamente o grupo `ADMIN`, o administrador recebe as notificações críticas e o relatório semanal por e-mail (SES), fora do fluxo autenticado da API. O grupo existe para representar esse papel e para uma eventual rota administrativa futura.

### Login

```
POST https://cognito-idp.<region>.amazonaws.com/
X-Amz-Target: AWSCognitoIdentityProviderService.InitiateAuth
```
```json
{
  "AuthFlow": "USER_PASSWORD_AUTH",
  "ClientId": "<UserPoolClientId>",
  "AuthParameters": { "USERNAME": "aluno@exemplo.com", "PASSWORD": "SenhaForte123!" }
}
```

Use o `IdToken` da resposta (`AuthenticationResult.IdToken`) no header, **sem** o prefixo `Bearer`:

```
Authorization: {{IdToken}}
```

---

# 🔌 API

| Método | Endpoint | Acesso |
|---|---|---|
| POST | `/avaliacao` | Grupo `ALUNO` |
| GET | `/aulas` | Qualquer usuário autenticado |

### `POST /avaliacao`

```json
{
  "descricao": "Aula excelente, muito bem explicada",
  "nota": 9,
  "aulaId": "aula-01"
}
```

| Status | Motivo |
|---|---|
| 201 | Avaliação registrada |
| 400 | Payload inválido (nota fora de 0-10, descrição/aulaId em branco, JSON malformado) |
| 401 | Sem token ou token inválido (rejeitado pelo Authorizer, antes da Lambda) |
| 403 | Usuário autenticado, mas fora do grupo `ALUNO` |
| 404 | `aulaId` não corresponde a nenhuma aula cadastrada |

### `GET /aulas`

Retorna a lista fixa de aulas: `[{ "id": "aula-01", "titulo": "..." }]`.

---

# 📨 Eventos e Processamento Assíncrono

```
┌─────────────────────┐        ┌──────────────────────────┐        ┌─────┐
│ SQS notificacoes    │──────▶ │ NotificarCriticoFunction │───────▶│ SES │──▶ e-mail ao admin
│ criticas (+ DLQ)    │        └──────────────────────────┘        └─────┘

┌──────────────────────┐        ┌────────────────────────┐
│ EventBridge          │───────▶│ GerarRelatorioSemanal  │──▶ S3 (CSV) + SES (e-mail)
│ (cron: toda segunda) │        └────────────────────────┘

┌─────────────────────────┐        ┌────────────────────────┐
│ Cognito                 │───────▶│ PosConfirmacaoFunction │──▶ AdminAddUserToGroup (ALUNO)
│ (trigger Post Confirm.) │        └────────────────────────┘
```

Três fluxos acontecem fora do caminho síncrono da API:

- **Notificação crítica**: a `ReceberAvaliacaoFunction` só publica na fila `sqs-notificacoes-criticas` quando a urgência é `ALTA`. A `NotificarCriticoFunction` consome essa fila e envia o e-mail ao admin via SES. Se o processamento de uma mensagem falhar 3 vezes consecutivas, ela vai para a `dlq-notificacoes-criticas`, sem bloquear o restante da fila.
- **Relatório semanal**: toda segunda-feira, o EventBridge dispara a `GerarRelatorioSemanalFunction`, que agrega os últimos 7 dias de avaliações (média de notas, contagem por dia, por urgência e por aula), salva um CSV no S3 e envia o mesmo conteúdo por e-mail.
- **Atribuição de grupo**: a confirmação de cadastro no Cognito dispara o trigger Post Confirmation, que chama a `PosConfirmacaoFunction`. Ela adiciona o usuário ao grupo `ALUNO` via `AdminAddUserToGroup`.

---

# 📧 Envio de E-mail (SES)

Remetente e destinatário são configuráveis separadamente, via contexto da CDK ou variável de ambiente:

| Papel | Contexto CDK | Variável de ambiente / secret | Usado em |
|---|---|---|---|
| Remetente | `remetenteEmail` | `SES_REMETENTE_EMAIL` | Cabeçalho `From` dos e-mails |
| Destinatário/admin | `adminEmail` | `ADMIN_EMAIL` | Notificação crítica, relatório semanal e alarmes do CloudWatch |

Se `remetenteEmail` não for informado, o remetente usa o mesmo endereço do destinatário (comportamento anterior à separação).

A conta AWS deste projeto está em modo *sandbox* do SES, o que exige que **cada endereço envolvido** (remetente e destinatário) esteja verificado individualmente antes de qualquer envio funcionar. A stack CDK cria uma `EmailIdentity` para cada um dos dois endereços (uma só, se forem o mesmo), e a AWS manda um e-mail com um link de confirmação para cada identidade criada. É preciso clicar nesse link antes do primeiro deploy funcional.

Na configuração atual do projeto, o remetente é o e-mail institucional da FIAP e o destinatário é um e-mail pessoal, e os dois foram verificados com sucesso.

---

# 📈 Monitoramento

CloudWatch Alarms em cada uma das 5 Lambdas (métrica de erros, limiar de 1 invocação com erro) e na DLQ de notificações críticas (mensagens presas). Todos os alarmes publicam no mesmo tópico SNS (`feedbackhub-alarmes`), que notifica o admin por e-mail.

---

# 🔐 Segurança e Governança de Acesso

- **Autenticação**: Amazon Cognito (ver seção [Autenticação e Autorização](#-autenticação-e-autorização)).
- **Autorização de borda**: o API Gateway valida o JWT via Cognito Authorizer antes de qualquer Lambda ser invocada. Requisições sem token ou com token inválido/expirado nunca chegam ao código da aplicação.
- **Autorização de negócio**: a distinção entre `ALUNO` e `ADMIN` é feita dentro da `ReceberAvaliacaoFunction`, lendo a claim `cognito:groups` do token validado.
- **IAM por função (least privilege)**: cada Lambda recebe só as permissões que usa de fato. Por exemplo, a `ReceberAvaliacaoFunction` tem escrita na tabela `Avaliacoes`, leitura na tabela `Aulas` e envio na fila de críticos, e a `GerarRelatorioSemanalFunction` só lê as duas tabelas e escreve no S3. Nenhuma Lambda tem permissão de administração do Cognito, exceto a `PosConfirmacaoFunction`.
- **Exceção deliberada (`Resource: "*"`)**: as permissões de SES (`ses:SendEmail`/`ses:SendRawEmail`) e de `cognito-idp:AdminAddUserToGroup` usam `Resource: "*"` em vez de uma ARN específica. Foi uma escolha consciente, não um descuido. No caso do Cognito, restringir pela ARN do próprio User Pool criava uma **dependência circular** no CloudFormation, já que o User Pool depende da Lambda via o trigger `addTrigger`, e a policy da Lambda dependeria do User Pool pela ARN. Abrimos o escopo da *ação* (mantendo-a restrita a essa única operação) para resolver o ciclo.
- **Nenhuma credencial fixa**: cada Lambda recebe só os **nomes** dos recursos (tabela, fila, bucket) via variável de ambiente, e a permissão de acesso vem da sua IAM Role.
- **Pipeline sem segredos de longa duração**: o GitHub Actions autentica na AWS via **OIDC** (`aws-actions/configure-aws-credentials`), sem access keys armazenadas como secret.

---

# 🧪 Testes

## Unitários e de integração

- **Unitários** (JUnit 5 + Mockito): regras de negócio isoladas (validação de aula, verificação de grupo, cálculo do relatório semanal, montagem do CSV e do e-mail), sem depender de nenhum serviço real.
- **Integração** (`*IT`, Testcontainers + LocalStack): validam a aplicação real contra DynamoDB, SQS, S3 e SES simulados.
- **Cognito não é coberto por testes de integração**: deixou de fazer parte do plano gratuito do LocalStack (exige o plano Base, pago), então a lógica que depende dele (checagem de grupo, trigger de Post Confirmation) é coberta só por testes unitários com mocks.

## Postman Collection

- **Arquivo**: [FeedbackHub.postman_collection.json](FeedbackHub.postman_collection.json)
- Cobre o fluxo completo de cadastro/login no Cognito (chamadas diretas à API do Cognito) e os endpoints `/avaliacao` e `/aulas`, com cenários de sucesso e erro para cada um.
- As requisições de login definem automaticamente as variáveis de coleção `{{token}}` (aluno) e `{{tokenAdmin}}` (admin) via script de teste, reaproveitadas nas demais requisições.

---

# 🏗️ Estrutura do Projeto

Dois projetos Gradle independentes no mesmo repositório: a aplicação (as 5 Lambdas) e a infraestrutura (CDK).

```
feedbackhub/
├── src/main/java/com/fiap/feedbackhub/
│   ├── cognito/           # Atribuição de grupo ao aluno (AtribuicaoGrupoAlunoService)
│   ├── config/             # Beans dos clients AWS (DynamoDB, SQS, S3, SES, Cognito)
│   ├── domain/
│   │   ├── converter/      # Conversores para o DynamoDB (ex: UrgenciaConverter)
│   │   ├── dto/             # DTOs de entrada/saída das Lambdas
│   │   ├── entity/          # Entidades DynamoDB (Avaliacao, Aula)
│   │   └── enumeration/     # Urgencia
│   ├── email/               # Montagem e envio de e-mails (SES)
│   ├── function/             # As 5 Lambdas (beans do Spring Cloud Function)
│   ├── messaging/            # Publicação na fila de notificações críticas
│   ├── report/                # Geração do CSV e upload no S3
│   ├── repository/            # Acesso ao DynamoDB (Avaliacao, Aula)
│   ├── service/                # Regras de negócio
│   └── support/                 # Helpers (resposta HTTP, validação, autorização)
├── src/test/java/com/fiap/feedbackhub/      # Testes unitários (*Test) e de integração (*IT)
├── Dockerfile                                 # Imagem única usada pelas 5 Lambdas
└── infra/
    └── src/main/java/com/fiap/feedbackhub/infra/
        ├── FeedbackhubApp.java                 # Entry point da CDK App
        └── FeedbackhubStack.java                # Toda a infraestrutura (CDK)
```

---

# 🎯 Objetivos da Arquitetura

- Cada Lambda com responsabilidade única (SRP), mesmo todas compartilhando a mesma imagem
- Zero servidores para gerenciar, com escala automática e custo por uso
- Infraestrutura inteiramente como código (CDK), sem passos manuais de provisionamento
- Deploy automatizado e repetível via pipeline, sem credenciais fixas (OIDC)
- Desacoplamento entre ingestão e notificação crítica via fila, com DLQ para falhas
- Autenticação/autorização centralizadas no Cognito, aplicadas na borda e reforçadas no código de negócio
- Aplicação observável: alarme de erro por função + DLQ, com notificação automática ao admin

---

# 📄 Licença
- Este projeto é acadêmico e de uso educacional.
