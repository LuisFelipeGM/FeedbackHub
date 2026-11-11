package com.fiap.feedbackhub.config;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

import java.util.Map;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SES;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public final class LocalStackContainerSingleton {

    private static final String TABELA_AVALIACOES = "Avaliacoes";
    private static final String FILA_NOTIFICACOES_CRITICAS = "sqs-notificacoes-criticas";
    private static final String FILA_NOTIFICACOES_CRITICAS_DLQ = "dlq-notificacoes-criticas";
    private static final String BUCKET_RELATORIOS = "feedbackhub-relatorios";
    private static final String EMAIL_REMETENTE = "admin@feedbackhub.local";

    private static final LocalStackContainer INSTANCE =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.8"))
                    .withServices(DYNAMODB, SQS, S3, SES);

    static {
        INSTANCE.start();
        criarTabelaAvaliacoes();
        criarFilaNotificacoesCriticas();
        criarBucketRelatorios();
        verificarIdentidadeSes();
    }

    private LocalStackContainerSingleton() {
    }

    public static LocalStackContainer getInstance() {
        return INSTANCE;
    }

    private static void criarTabelaAvaliacoes() {
        try (DynamoDbClient dynamoDbClient = dynamoDbClient()) {
            dynamoDbClient.createTable(CreateTableRequest.builder()
                    .tableName(TABELA_AVALIACOES)
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("data").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.S).build())
                    .keySchema(
                            KeySchemaElement.builder().attributeName("data").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("id").keyType(KeyType.RANGE).build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build());
        }
    }

    private static void criarFilaNotificacoesCriticas() {
        try (SqsClient sqsClient = sqsClient()) {
            String dlqUrl = sqsClient.createQueue(CreateQueueRequest.builder()
                            .queueName(FILA_NOTIFICACOES_CRITICAS_DLQ)
                            .build())
                    .queueUrl();

            String dlqArn = sqsClient.getQueueAttributes(GetQueueAttributesRequest.builder()
                            .queueUrl(dlqUrl)
                            .attributeNames(QueueAttributeName.QUEUE_ARN)
                            .build())
                    .attributes()
                    .get(QueueAttributeName.QUEUE_ARN);

            sqsClient.createQueue(CreateQueueRequest.builder()
                    .queueName(FILA_NOTIFICACOES_CRITICAS)
                    .attributes(Map.of(
                            QueueAttributeName.REDRIVE_POLICY,
                            "{\"deadLetterTargetArn\":\"" + dlqArn + "\",\"maxReceiveCount\":\"3\"}"))
                    .build());
        }
    }

    private static void criarBucketRelatorios() {
        try (S3Client s3Client = s3Client()) {
            s3Client.createBucket(builder -> builder.bucket(BUCKET_RELATORIOS));
        }
    }

    private static void verificarIdentidadeSes() {
        try (SesClient sesClient = sesClient()) {
            sesClient.verifyEmailIdentity(VerifyEmailIdentityRequest.builder()
                    .emailAddress(EMAIL_REMETENTE)
                    .build());
        }
    }

    private static DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(INSTANCE.getEndpointOverride(DYNAMODB))
                .region(Region.of(INSTANCE.getRegion()))
                .credentialsProvider(credenciais())
                .build();
    }

    private static SqsClient sqsClient() {
        return SqsClient.builder()
                .endpointOverride(INSTANCE.getEndpointOverride(SQS))
                .region(Region.of(INSTANCE.getRegion()))
                .credentialsProvider(credenciais())
                .build();
    }

    private static S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(INSTANCE.getEndpointOverride(S3))
                .region(Region.of(INSTANCE.getRegion()))
                .credentialsProvider(credenciais())
                .forcePathStyle(true)
                .build();
    }

    private static SesClient sesClient() {
        return SesClient.builder()
                .endpointOverride(INSTANCE.getEndpointOverride(SES))
                .region(Region.of(INSTANCE.getRegion()))
                .credentialsProvider(credenciais())
                .build();
    }

    private static StaticCredentialsProvider credenciais() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(INSTANCE.getAccessKey(), INSTANCE.getSecretKey()));
    }
}
