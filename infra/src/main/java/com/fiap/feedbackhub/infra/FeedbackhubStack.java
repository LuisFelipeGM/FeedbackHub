package com.fiap.feedbackhub.infra;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.ses.EmailIdentity;
import software.amazon.awscdk.services.ses.Identity;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.constructs.Construct;

public class FeedbackhubStack extends Stack {

    private static final String EMAIL_ADMIN_PADRAO = "admin@feedbackhub.local";

    private final Table tabelaAvaliacoes;
    private final Queue filaNotificacoesCriticas;
    private final Queue filaNotificacoesCriticasDlq;
    private final Bucket bucketRelatorios;

    public FeedbackhubStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public FeedbackhubStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.tabelaAvaliacoes = criarTabelaAvaliacoes();
        this.filaNotificacoesCriticasDlq = criarFilaNotificacoesCriticasDlq();
        this.filaNotificacoesCriticas = criarFilaNotificacoesCriticas(filaNotificacoesCriticasDlq);
        this.bucketRelatorios = criarBucketRelatorios();
        criarIdentidadeSes();
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
                .identity(Identity.email(resolverEmailAdmin()))
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
}
