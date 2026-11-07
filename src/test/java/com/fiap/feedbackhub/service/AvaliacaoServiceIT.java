package com.fiap.feedbackhub.service;

import com.fiap.feedbackhub.config.AbstractIT;
import com.fiap.feedbackhub.domain.dto.AvaliacaoDTO;
import com.fiap.feedbackhub.domain.entity.Avaliacao;
import com.fiap.feedbackhub.repository.AvaliacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AvaliacaoServiceIT extends AbstractIT {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Value("${aws.dynamodb.tabela-avaliacoes}")
    private String tabelaAvaliacoes;

    @Value("${aws.sqs.fila-notificacoes-criticas}")
    private String filaNome;

    @BeforeEach
    void limparRecursos() {
        DynamoDbTable<Avaliacao> tabela = enhancedClient.table(tabelaAvaliacoes, TableSchema.fromBean(Avaliacao.class));
        tabela.scan().items().forEach(tabela::deleteItem);

        List<Message> mensagens;
        do {
            mensagens = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                            .queueUrl(filaUrl())
                            .maxNumberOfMessages(10)
                            .build())
                    .messages();
            mensagens.forEach(mensagem -> sqsClient.deleteMessage(
                    builder -> builder.queueUrl(filaUrl()).receiptHandle(mensagem.receiptHandle())));
        } while (!mensagens.isEmpty());
    }

    @Test
    void devePersistirENaoPublicarQuandoUrgenciaNaoForAlta() {
        avaliacaoService.registrar(new AvaliacaoDTO(9, "Aula excelente"));

        List<Avaliacao> encontradas = avaliacaoRepository.buscarPorData(LocalDate.now());
        assertThat(encontradas).hasSize(1);
        assertThat(receberMensagens()).isEmpty();
    }

    @Test
    void devePersistirEPublicarQuandoUrgenciaForAlta() {
        avaliacaoService.registrar(new AvaliacaoDTO(1, "Aula muito ruim, professor não apareceu"));

        List<Avaliacao> encontradas = avaliacaoRepository.buscarPorData(LocalDate.now());
        assertThat(encontradas).hasSize(1);

        List<Message> mensagens = receberMensagens();
        assertThat(mensagens).hasSize(1);
        assertThat(mensagens.getFirst().body())
                .contains("ALTA")
                .contains("Aula muito ruim");
    }

    private List<Message> receberMensagens() {
        return sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(filaUrl())
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(1)
                        .build())
                .messages();
    }

    private String filaUrl() {
        return sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(filaNome).build()).queueUrl();
    }
}
