package com.fiap.feedbackhub.infra;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.AuthorizationType;
import software.amazon.awscdk.services.apigateway.CognitoUserPoolsAuthorizer;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.MethodOptions;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.cloudwatch.Alarm;
import software.amazon.awscdk.services.cloudwatch.actions.SnsAction;
import software.amazon.awscdk.services.cognito.AccountRecovery;
import software.amazon.awscdk.services.cognito.AuthFlow;
import software.amazon.awscdk.services.cognito.AutoVerifiedAttrs;
import software.amazon.awscdk.services.cognito.CfnUserPoolGroup;
import software.amazon.awscdk.services.cognito.PasswordPolicy;
import software.amazon.awscdk.services.cognito.SignInAliases;
import software.amazon.awscdk.services.cognito.StandardAttribute;
import software.amazon.awscdk.services.cognito.StandardAttributes;
import software.amazon.awscdk.services.cognito.UserPool;
import software.amazon.awscdk.services.cognito.UserPoolClient;
import software.amazon.awscdk.services.cognito.UserPoolOperation;
import software.amazon.awscdk.customresources.AwsCustomResource;
import software.amazon.awscdk.customresources.AwsCustomResourcePolicy;
import software.amazon.awscdk.customresources.AwsSdkCall;
import software.amazon.awscdk.customresources.PhysicalResourceId;
import software.amazon.awscdk.customresources.SdkCallsPolicyOptions;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.DockerImageCode;
import software.amazon.awscdk.services.lambda.DockerImageFunction;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.eventsources.SqsEventSource;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.ses.EmailIdentity;
import software.amazon.awscdk.services.ses.Identity;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackhubStack extends Stack {

    private static final String EMAIL_ADMIN_PADRAO = "admin@feedbackhub.local";

    private final Table tabelaAvaliacoes;
    private final Table tabelaAulas;
    private final Queue filaNotificacoesCriticas;
    private final Queue filaNotificacoesCriticasDlq;
    private final Bucket bucketRelatorios;
    private final String emailAdmin;
    private final DockerImageCode imagemLambda;

    private UserPool userPool;
    private UserPoolClient userPoolClient;

    private DockerImageFunction funcaoReceberAvaliacao;
    private DockerImageFunction funcaoNotificarCritico;
    private DockerImageFunction funcaoGerarRelatorioSemanal;
    private DockerImageFunction funcaoPosConfirmacao;
    private DockerImageFunction funcaoListarAulas;

    public FeedbackhubStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public FeedbackhubStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.emailAdmin = resolverEmailAdmin();
        this.imagemLambda = DockerImageCode.fromImageAsset("..");

        this.tabelaAvaliacoes = criarTabelaAvaliacoes();
        this.tabelaAulas = criarTabelaAulas();
        semearTabelaAulas(tabelaAulas);
        this.filaNotificacoesCriticasDlq = criarFilaNotificacoesCriticasDlq();
        this.filaNotificacoesCriticas = criarFilaNotificacoesCriticas(filaNotificacoesCriticasDlq);
        this.bucketRelatorios = criarBucketRelatorios();
        criarIdentidadeSes();
        criarUserPool();

        criarFuncaoReceberAvaliacao();
        criarFuncaoNotificarCritico();
        criarFuncaoGerarRelatorioSemanal();
        criarFuncaoPosConfirmacao();
        criarFuncaoListarAulas();

        criarApiGateway();
        criarAgendamentoRelatorioSemanal();
        criarMonitoramento();
    }

    private Table criarTabelaAvaliacoes() {
        return Table.Builder.create(this, "TabelaAvaliacoes")
                .tableName("Avaliacoes")
                .partitionKey(Attribute.builder().name("data").type(AttributeType.STRING).build())
                .sortKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private Table criarTabelaAulas() {
        return Table.Builder.create(this, "TabelaAulas")
                .tableName("Aulas")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private void semearTabelaAulas(Table tabela) {
        AwsSdkCall seedCall = AwsSdkCall.builder()
                .service("DynamoDB")
                .action("batchWriteItem")
                .parameters(Map.of(
                        "RequestItems", Map.of(
                                tabela.getTableName(), List.of(
                                        itemAula("aula-01", "Introdução à Arquitetura Serverless"),
                                        itemAula("aula-02", "AWS Lambda e Funções de Responsabilidade Única"),
                                        itemAula("aula-03", "Mensageria com SQS, SNS e EventBridge")))))
                .physicalResourceId(PhysicalResourceId.of("SeedTabelaAulas"))
                .build();

        AwsCustomResource seed = AwsCustomResource.Builder.create(this, "SeedTabelaAulas")
                .onCreate(seedCall)
                .policy(AwsCustomResourcePolicy.fromSdkCalls(SdkCallsPolicyOptions.builder()
                        .resources(List.of(tabela.getTableArn()))
                        .build()))
                .build();

        seed.getNode().addDependency(tabela);
    }

    private Map<String, Object> itemAula(String id, String titulo) {
        return Map.of(
                "PutRequest", Map.of(
                        "Item", Map.of(
                                "id", Map.of("S", id),
                                "titulo", Map.of("S", titulo))));
    }

    private Queue criarFilaNotificacoesCriticasDlq() {
        return Queue.Builder.create(this, "FilaNotificacoesCriticasDlq")
                .queueName("dlq-notificacoes-criticas")
                .retentionPeriod(Duration.days(14))
                .build();
    }

    private Queue criarFilaNotificacoesCriticas(Queue dlq) {
        return Queue.Builder.create(this, "FilaNotificacoesCriticas")
                .queueName("sqs-notificacoes-criticas")
                .deadLetterQueue(DeadLetterQueue.builder()
                        .queue(dlq)
                        .maxReceiveCount(3)
                        .build())
                .build();
    }

    private Bucket criarBucketRelatorios() {
        return Bucket.Builder.create(this, "BucketRelatorios")
                .encryption(BucketEncryption.S3_MANAGED)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build();
    }

    private void criarIdentidadeSes() {
        EmailIdentity.Builder.create(this, "IdentidadeSesAdmin")
                .identity(Identity.email(emailAdmin))
                .build();
    }

    private void criarUserPool() {
        userPool = UserPool.Builder.create(this, "UserPoolFeedbackhub")
                .userPoolName("feedbackhub-users")
                .selfSignUpEnabled(true)
                .signInAliases(SignInAliases.builder().email(true).build())
                .autoVerify(AutoVerifiedAttrs.builder().email(true).build())
                .standardAttributes(StandardAttributes.builder()
                        .email(StandardAttribute.builder().required(true).mutable(true).build())
                        .build())
                .passwordPolicy(PasswordPolicy.builder()
                        .minLength(8)
                        .requireLowercase(true)
                        .requireUppercase(true)
                        .requireDigits(true)
                        .requireSymbols(false)
                        .build())
                .accountRecovery(AccountRecovery.EMAIL_ONLY)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        userPoolClient = UserPoolClient.Builder.create(this, "UserPoolClientFeedbackhub")
                .userPool(userPool)
                .generateSecret(false)
                .authFlows(AuthFlow.builder().userPassword(true).build())
                .build();

        CfnUserPoolGroup.Builder.create(this, "GrupoAdmin")
                .userPoolId(userPool.getUserPoolId())
                .groupName("ADMIN")
                .build();

        CfnUserPoolGroup.Builder.create(this, "GrupoAluno")
                .userPoolId(userPool.getUserPoolId())
                .groupName("ALUNO")
                .build();

        CfnOutput.Builder.create(this, "UserPoolId")
                .value(userPool.getUserPoolId())
                .build();

        CfnOutput.Builder.create(this, "UserPoolClientId")
                .value(userPoolClient.getUserPoolClientId())
                .build();
    }

    private String resolverEmailAdmin() {
        Object emailContexto = this.getNode().tryGetContext("adminEmail");
        if (emailContexto != null) {
            return emailContexto.toString();
        }

        String emailAmbiente = System.getenv("ADMIN_EMAIL");
        if (emailAmbiente != null && !emailAmbiente.isBlank()) {
            return emailAmbiente;
        }

        return EMAIL_ADMIN_PADRAO;
    }

    private void criarFuncaoReceberAvaliacao() {
        funcaoReceberAvaliacao = DockerImageFunction.Builder.create(this, "ReceberAvaliacaoFunction")
                .code(imagemLambda)
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .environment(variaveisAmbienteComuns())
                .build();

        funcaoReceberAvaliacao.addEnvironment("SPRING_CLOUD_FUNCTION_DEFINITION", "receberAvaliacao");

        tabelaAvaliacoes.grantWriteData(funcaoReceberAvaliacao);
        tabelaAulas.grantReadData(funcaoReceberAvaliacao);
        filaNotificacoesCriticas.grantSendMessages(funcaoReceberAvaliacao);
    }

    private void criarFuncaoNotificarCritico() {
        funcaoNotificarCritico = DockerImageFunction.Builder.create(this, "NotificarCriticoFunction")
                .code(imagemLambda)
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .environment(variaveisAmbienteComuns())
                .build();

        funcaoNotificarCritico.addEnvironment("SPRING_CLOUD_FUNCTION_DEFINITION", "notificarCritico");

        funcaoNotificarCritico.addEventSource(SqsEventSource.Builder.create(filaNotificacoesCriticas)
                .batchSize(10)
                .build());

        concederPermissaoSes(funcaoNotificarCritico);
    }

    private void criarFuncaoGerarRelatorioSemanal() {
        funcaoGerarRelatorioSemanal = DockerImageFunction.Builder.create(this, "GerarRelatorioSemanalFunction")
                .code(imagemLambda)
                .memorySize(512)
                .timeout(Duration.seconds(60))
                .environment(variaveisAmbienteComuns())
                .build();

        funcaoGerarRelatorioSemanal.addEnvironment("SPRING_CLOUD_FUNCTION_DEFINITION", "gerarRelatorioSemanal");

        tabelaAvaliacoes.grantReadData(funcaoGerarRelatorioSemanal);
        tabelaAulas.grantReadData(funcaoGerarRelatorioSemanal);
        bucketRelatorios.grantWrite(funcaoGerarRelatorioSemanal);
        concederPermissaoSes(funcaoGerarRelatorioSemanal);
    }

    private void criarFuncaoPosConfirmacao() {
        funcaoPosConfirmacao = DockerImageFunction.Builder.create(this, "PosConfirmacaoFunction")
                .code(imagemLambda)
                .memorySize(1024)
                .timeout(Duration.seconds(10))
                .environment(variaveisAmbienteComuns())
                .build();

        funcaoPosConfirmacao.addEnvironment("SPRING_CLOUD_FUNCTION_DEFINITION", "posConfirmacao");

        funcaoPosConfirmacao.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("cognito-idp:AdminAddUserToGroup"))
                .resources(List.of("*"))
                .build());

        userPool.addTrigger(UserPoolOperation.POST_CONFIRMATION, funcaoPosConfirmacao);
    }

    private void criarFuncaoListarAulas() {
        funcaoListarAulas = DockerImageFunction.Builder.create(this, "ListarAulasFunction")
                .code(imagemLambda)
                .memorySize(512)
                .timeout(Duration.seconds(10))
                .environment(variaveisAmbienteComuns())
                .build();

        funcaoListarAulas.addEnvironment("SPRING_CLOUD_FUNCTION_DEFINITION", "listarAulas");

        tabelaAulas.grantReadData(funcaoListarAulas);
    }

    private void criarApiGateway() {
        RestApi restApi = RestApi.Builder.create(this, "FeedbackhubRestApi")
                .restApiName("feedbackhub-api")
                .build();

        CognitoUserPoolsAuthorizer autorizadorCognito = CognitoUserPoolsAuthorizer.Builder.create(this, "AutorizadorCognito")
                .cognitoUserPools(List.of(userPool))
                .build();

        Resource recursoAvaliacao = restApi.getRoot().addResource("avaliacao");
        recursoAvaliacao.addMethod("POST", new LambdaIntegration(funcaoReceberAvaliacao), MethodOptions.builder()
                .authorizationType(AuthorizationType.COGNITO)
                .authorizer(autorizadorCognito)
                .build());

        Resource recursoAulas = restApi.getRoot().addResource("aulas");
        recursoAulas.addMethod("GET", new LambdaIntegration(funcaoListarAulas), MethodOptions.builder()
                .authorizationType(AuthorizationType.COGNITO)
                .authorizer(autorizadorCognito)
                .build());

        CfnOutput.Builder.create(this, "UrlApi")
                .value(restApi.getUrl())
                .build();
    }

    private void criarAgendamentoRelatorioSemanal() {
        Rule.Builder.create(this, "GerarRelatorioSemanalSchedule")
                .schedule(Schedule.cron(CronOptions.builder()
                        .weekDay("MON")
                        .hour("11")
                        .minute("0")
                        .build()))
                .targets(List.of(new LambdaFunction(funcaoGerarRelatorioSemanal)))
                .build();
    }

    private void criarMonitoramento() {
        Topic topicoAlarmes = Topic.Builder.create(this, "TopicoAlarmesOperacionais")
                .topicName("feedbackhub-alarmes")
                .build();

        topicoAlarmes.addSubscription(new EmailSubscription(emailAdmin));

        criarAlarmeErros(funcaoReceberAvaliacao, "ReceberAvaliacao", topicoAlarmes);
        criarAlarmeErros(funcaoNotificarCritico, "NotificarCritico", topicoAlarmes);
        criarAlarmeErros(funcaoGerarRelatorioSemanal, "GerarRelatorioSemanal", topicoAlarmes);
        criarAlarmeErros(funcaoPosConfirmacao, "PosConfirmacao", topicoAlarmes);
        criarAlarmeErros(funcaoListarAulas, "ListarAulas", topicoAlarmes);
        criarAlarmeDlq(topicoAlarmes);
    }

    private void criarAlarmeErros(IFunction funcao, String nomeFuncao, Topic topico) {
        Alarm alarme = Alarm.Builder.create(this, "Alarme" + nomeFuncao + "Erros")
                .metric(funcao.metricErrors())
                .threshold(1)
                .evaluationPeriods(1)
                .alarmDescription("Erros na Lambda " + nomeFuncao)
                .build();

        alarme.addAlarmAction(new SnsAction(topico));
    }

    private void criarAlarmeDlq(Topic topico) {
        Alarm alarme = Alarm.Builder.create(this, "AlarmeDlqNotificacoesCriticas")
                .metric(filaNotificacoesCriticasDlq.metricApproximateNumberOfMessagesVisible())
                .threshold(1)
                .evaluationPeriods(1)
                .alarmDescription("Mensagens paradas na DLQ de notificacoes criticas")
                .build();

        alarme.addAlarmAction(new SnsAction(topico));
    }

    private Map<String, String> variaveisAmbienteComuns() {
        Map<String, String> variaveis = new HashMap<>();
        variaveis.put("DYNAMODB_TABLE_AVALIACOES", tabelaAvaliacoes.getTableName());
        variaveis.put("DYNAMODB_TABLE_AULAS", tabelaAulas.getTableName());
        variaveis.put("SQS_QUEUE_NOTIFICACOES_CRITICAS", filaNotificacoesCriticas.getQueueName());
        variaveis.put("S3_BUCKET_RELATORIOS", bucketRelatorios.getBucketName());
        variaveis.put("SES_REMETENTE", emailAdmin);
        variaveis.put("SES_DESTINATARIO_ADMIN", emailAdmin);
        variaveis.put("TZ", "America/Sao_Paulo");
        return variaveis;
    }

    private void concederPermissaoSes(IFunction funcao) {
        funcao.addToRolePolicy(PolicyStatement.Builder.create()
                .effect(Effect.ALLOW)
                .actions(List.of("ses:SendEmail", "ses:SendRawEmail"))
                .resources(List.of("*"))
                .build());
    }
}
